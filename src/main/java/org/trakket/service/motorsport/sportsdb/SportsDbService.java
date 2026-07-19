package org.trakket.service.motorsport.sportsdb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.trakket.dto.motorsport.thesportsdb.SportsDbEventDto;
import org.trakket.dto.motorsport.thesportsdb.SportsDbEventsSeasonResponse;
import org.trakket.dto.motorsport.thesportsdb.SportsDbMetadata;
import org.trakket.enums.EventStatus;
import org.trakket.enums.ExternalMotorsportSource;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.model.MotorsportEvent;
import org.trakket.repository.MotorsportEventRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to import season events from TheSportsDB.
 */
@Service
@Slf4j
public class SportsDbService {

    private final WebClient sportsDbWebClient;
    private final MotorsportEventRepository eventRepository;

    public SportsDbService(@Qualifier("sportsDbWebClient") WebClient sportsDbWebClient,
                           MotorsportEventRepository eventRepository) {
        this.sportsDbWebClient = sportsDbWebClient;
        this.eventRepository = eventRepository;
    }

    private static final Pattern WINNER_LINE = Pattern.compile("(?m)^[ \\t]*1\\s*/([^/\\r\\n]+)");

    // Sub-session keywords used to filter out non-race sessions (practice, qualifying, etc.)
    private static final Pattern SUB_SESSION_PATTERN = Pattern.compile(
            "(?i)(free practice|qualifying|hyperpole|prologue|sprint race|warm.?up|warmup|shakedown)"
    );

    // Max rounds to iterate; iteration stops early when a round returns no events
    private static final int MAX_ROUNDS = 30;

    // Events whose datetime is more than this far in the past are marked COMPLETED
    // even if the API returns no result data (e.g. free tier limitations)
    private static final long COMPLETED_THRESHOLD_HOURS = 24;

    private final Map<MotorsportCompetition, SportsDbMetadata> competitionMapping = Map.ofEntries(
            Map.entry(MotorsportCompetition.WORLD_ENDURANCE_CHAMPIONSHIP, new SportsDbMetadata(4413, "2026")),
            Map.entry(MotorsportCompetition.MOTOGP,  new SportsDbMetadata(4407, "2026")),
            Map.entry(MotorsportCompetition.FORMULA_E,  new SportsDbMetadata(4371, "2025-2026"))
    );

    public void syncEvents(MotorsportCompetition competition) {
        importSeasonEvents(competition);
    }

    public void importSeasonEvents(MotorsportCompetition competition) {
        if (competition == null) {
            log.error("Unable to import season events - competition is null");
            throw new IllegalArgumentException("Competition is null");
        }
        SportsDbMetadata metadata = competitionMapping.get(competition);
        if (metadata == null) {
            log.error("Unable to import season events - no metadata found for competition {}", competition);
            throw new RuntimeException("No metadata found for competition");
        }
        Integer leagueId = metadata.getCompetitionId();
        String season = metadata.getSeason();
        log.info("Importing {} events for season {} from TheSportsDB", competition.getDisplayName(), season);

        int importedCount = 0;
        int skippedCount = 0;
        int lastRound = 0;

        for (int r = 1; r <= MAX_ROUNDS; r++) {
            final int round = r;
            SportsDbEventsSeasonResponse resp;
            try {
                resp = sportsDbWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/eventsround.php")
                                .queryParam("id", leagueId)
                                .queryParam("s", season)
                                .queryParam("r", round)
                                .build())
                        .retrieve()
                        .onStatus(HttpStatusCode::isError,
                                response -> response.bodyToMono(String.class)
                                        .flatMap(body -> Mono.error(new RuntimeException("TheSportsDB round error: " + body))))
                        .bodyToMono(SportsDbEventsSeasonResponse.class)
                        .block();
            } catch (Exception e) {
                log.warn("Failed to fetch {} round {}: {}", competition.getDisplayName(), round, e.getMessage());
                break;
            }

            if (resp == null || resp.getEvents() == null || resp.getEvents().isEmpty()) {
                log.debug("No more events for {} after round {}", competition.getDisplayName(), round - 1);
                break;
            }

            lastRound = round;

            for (SportsDbEventDto dto : resp.getEvents()) {
                String name = dto.getStrEvent();
                if (name == null || SUB_SESSION_PATTERN.matcher(name).find()) {
                    skippedCount++;
                    continue;
                }

                log.info("Processing {} round {}: \"{}\" {}",
                        competition.getDisplayName(), round, name,
                        dto.getStrResult() != null && !dto.getStrResult().isBlank() ? "(has result)" : "");

                try {
                    upsertFromDto(dto, competition);
                    importedCount++;
                } catch (Exception e) {
                    log.error("Failed to import {} round {} \"{}\": {}",
                            competition.getDisplayName(), round, name, e.getMessage());
                }
            }
        }

        log.info("Finished importing {} season {}: {} main races imported, {} sub-sessions skipped (rounds 1-{})",
                competition.getDisplayName(), season, importedCount, skippedCount, lastRound);
    }

    private void upsertFromDto(SportsDbEventDto dto, MotorsportCompetition competition) {
        Integer round = parseIntSafe(dto.getIntRound());
        Integer season = parseIntSafe(dto.getStrSeason());
        if (round == null || season == null) {
            log.warn("Skipping {} event \"{}\" (id={}) due to missing round or season",
                    competition.getDisplayName(), dto.getStrEvent(), dto.getIdEvent());
            return;
        }

        Optional<MotorsportEvent> existing = eventRepository.findByCompetitionAndSeasonAndRound(
                competition, season, round);

        boolean isNew = existing.isEmpty();
        MotorsportEvent event = existing.orElseGet(MotorsportEvent::new);

        // do not update completed events unless we have result data (e.g. winner) to set
        if (!isNew && event.getStatus() == EventStatus.COMPLETED
                && (dto.getStrResult() == null || dto.getStrResult().isBlank())) {
            log.info("Skipping {} round {} \"{}\" - already COMPLETED with no new result data",
                    competition.getDisplayName(), round, dto.getStrEvent());
            return;
        }

        event.setCompetition(competition);
        event.setSeason(season);
        event.setRaceName(dto.getStrEvent());
        event.setCircuitName(dto.getStrVenue());

        event.setExternalSource(ExternalMotorsportSource.THESPORTSDB);
        event.setExternalSourceId(parseLongSafe(dto.getIdEvent()));
        event.setFlagUrl(dto.getStrLeagueBadge());

        // base Event fields
        event.setRound(round);
        event.setLocation(dto.getStrVenue());
        event.setDateTime(parseDateTime(dto.getStrTimestamp()));
        event.setLastUpdated(LocalDateTime.now());

        // status handling
        boolean hasResult = dto.getStrResult() != null && !dto.getStrResult().isBlank();
        LocalDateTime eventDateTime = parseDateTime(dto.getStrTimestamp());
        boolean isPastEvent = eventDateTime != null
                && eventDateTime.isBefore(LocalDateTime.now().minusHours(COMPLETED_THRESHOLD_HOURS));

        if (hasResult) {
            event.setStatus(EventStatus.COMPLETED);
            String winner = parseWinner(dto.getStrResult());
            event.setWinner(winner);
        } else if (isPastEvent) {
            // API may not return results for past events (free tier), but they are completed
            event.setStatus(EventStatus.COMPLETED);
        } else {
            event.setStatus(EventStatus.SCHEDULED);
        }

        eventRepository.save(event);

        if (isNew) {
            String statusLabel = hasResult ? "COMPLETED" : (isPastEvent ? "COMPLETED (no result data)" : "SCHEDULED");
            log.info("Inserted {} round {} '{}' as {}",
                    competition.getDisplayName(), round, dto.getStrEvent(), statusLabel);
        } else {
            String updateMsg;
            if (hasResult) {
                updateMsg = "-> COMPLETED (winner: " + event.getWinner() + ")";
            } else if (isPastEvent) {
                updateMsg = "-> COMPLETED (event date in the past)";
            } else {
                updateMsg = "(SCHEDULED)";
            }
            log.info("Updated {} round {} '{}' {}",
                    competition.getDisplayName(), round, dto.getStrEvent(), updateMsg);
        }
    }

    private String parseWinner(String result) {
        if (result == null) return null;
        Matcher m = WINNER_LINE.matcher(result);
        return m.find() ? m.group(1).trim() : null;
    }

    private Long parseLongSafe(String s) {
        try {
            return s != null ? Long.parseLong(s) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntSafe(String s) {
        try {
            return s != null ? Integer.parseInt(s) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String ts) {
        if (ts == null || ts.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(ts);
        } catch (Exception e) {
            return null;
        }
    }
}
