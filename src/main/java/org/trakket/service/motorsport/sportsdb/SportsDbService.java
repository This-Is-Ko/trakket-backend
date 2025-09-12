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

    // Due to restrictions on TheSportsDb free, only competitions with less than 15 events in a season should use this
    private final Map<MotorsportCompetition, SportsDbMetadata> competitionMapping = Map.ofEntries(
            Map.entry(MotorsportCompetition.WORLD_ENDURANCE_CHAMPIONSHIP, new SportsDbMetadata(4413, "2025")),
            Map.entry(MotorsportCompetition.MOTOGP,  new SportsDbMetadata(4407, "2025")),
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
        log.info("Importing {} events", competition.getDisplayName());
        SportsDbMetadata metadata = competitionMapping.get(competition);
        if (metadata == null) {
            log.error("Unable to import season events - no metadata found for competition {}", competition);
            throw new RuntimeException("No metadata found for competition");
        }
        Integer leagueId = metadata.getCompetitionId();
        SportsDbEventsSeasonResponse resp = sportsDbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/eventsseason.php")
                        .queryParam("id", leagueId)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("TheSportsDB error: " + body))))
                .bodyToMono(SportsDbEventsSeasonResponse.class)
                .block();

        if (resp == null || resp.getEvents() == null) {
            log.info("No {} events found in TheSportsDb", competition.getDisplayName());
            throw new RuntimeException("No TheSportsDb events found");
        }

        for (SportsDbEventDto dto : resp.getEvents()) {
            try {
                upsertFromDto(dto, competition);
            } catch (Exception e) {
                log.error("Failed to import {} event {}: {}", competition.getDisplayName(), dto.getIdEvent(), e.getMessage());
            }
        }
    }

    private void upsertFromDto(SportsDbEventDto dto, MotorsportCompetition competition) {
        Long extId = parseLongSafe(dto.getIdEvent());
        Optional<MotorsportEvent> existing = eventRepository.findByExternalSourceAndExternalSourceId(
                ExternalMotorsportSource.THESPORTSDB, extId);

        MotorsportEvent event = existing.orElseGet(MotorsportEvent::new);

        if (event.getStatus() == EventStatus.COMPLETED) {
            return;
        }

        event.setCompetition(competition);
        event.setSeason(parseIntSafe(dto.getStrSeason()));
        event.setRaceName(dto.getStrEvent());
        event.setCircuitName(dto.getStrVenue());

        event.setExternalSource(ExternalMotorsportSource.THESPORTSDB);
        event.setExternalSourceId(extId);
        event.setFlagUrl(dto.getStrLeagueBadge());

        // base Event fields
        event.setRound(parseIntSafe(dto.getIntRound()));
        event.setLocation(dto.getStrVenue());
        event.setDateTime(parseDateTime(dto.getStrTimestamp()));
        event.setLastUpdated(LocalDateTime.now());

        // status handling
        if (dto.getStrResult() != null && !dto.getStrResult().isBlank()) {
            event.setStatus(EventStatus.COMPLETED);
            event.setWinner(parseWinner(dto.getStrResult()));
        } else {
            event.setStatus(EventStatus.SCHEDULED);
        }

        eventRepository.save(event);
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
