package org.trakket.service.football.sofascore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trakket.client.SofascoreClient;
import org.trakket.dto.football.sofascore.SofascoreEventDto;
import org.trakket.dto.football.sofascore.SofascoreMetadata;
import org.trakket.dto.football.sofascore.SofascoreRoundsResponse;
import org.trakket.dto.football.sofascore.SofascoreStatusDto;
import org.trakket.dto.football.sofascore.SofascoreTeamDto;
import org.trakket.enums.EventStatus;
import org.trakket.enums.ExternalFootballSource;
import org.trakket.enums.FootballCompetition;
import org.trakket.model.FootballEvent;
import org.trakket.model.FootballTeam;
import org.trakket.repository.FootballEventRepository;
import org.trakket.repository.FootballTeamRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SofascoreEventSyncService {

    private final SofascoreClient client;
    private final FootballEventRepository footballEventRepository;
    private final FootballTeamRepository footballTeamRepository;

    private final Map<FootballCompetition, SofascoreMetadata> competitionMapping = Map.ofEntries(
            Map.entry(FootballCompetition.ENGLISH_PREMIER_LEAGUE, new SofascoreMetadata(17, 76986)),
            Map.entry(FootballCompetition.UEFA_CHAMPIONS_LEAGUE, new SofascoreMetadata(7, null)),
            Map.entry(FootballCompetition.LA_LIGA, new SofascoreMetadata(8, null)),
            Map.entry(FootballCompetition.SERIE_A, new SofascoreMetadata(23, null)),
            Map.entry(FootballCompetition.BUNDESLIGA, new SofascoreMetadata(35, null)),
            Map.entry(FootballCompetition.ENGLISH_WOMENS_SUPER_LEAGUE, new SofascoreMetadata(1044, 79227))
    );

    public void syncEvents() {
        syncRound(FootballCompetition.ENGLISH_PREMIER_LEAGUE, 3);
    }

    /**
     * Add/update round events for a tournament and season.
     */
    public void syncRound(FootballCompetition competition, Integer round) {
        SofascoreMetadata metadata = getCompetitionMetadata(competition);
        Integer uniqueTournamentId = metadata.getCompetitionId();
        Integer seasonId = metadata.getSeasonId();
        if (uniqueTournamentId == null || seasonId == null) {
            throw new IllegalArgumentException("Missing competition metadata: " + competition);
        }

        if (round == null) {
            SofascoreRoundsResponse roundsResponse = client.fetchRounds(uniqueTournamentId, seasonId).block();
            if (roundsResponse == null || roundsResponse.getCurrentRound() == null || roundsResponse.getRounds() == null) {
                throw new RuntimeException("No rounds found for " + competition);
            }
            round = roundsResponse.getCurrentRound().getRound();
            log.info("Current round for {} is {}", competition, round);
        }

        client.fetchRoundEvents(uniqueTournamentId, seasonId, round)
                .flatMapMany(resp -> Flux.fromIterable(resp.getEvents()))
                // do DB work on boundedElastic
                .flatMap(dto -> Mono.fromCallable(() -> upsertFromSofaEvent(dto, competition))
                        .subscribeOn(Schedulers.boundedElastic()))
                .doOnNext(e -> log.info("Upserted event {}", e.getId()))
                .doOnError(ex -> log.error("SofaScore sync error", ex))
                .subscribe();
    }

    @Transactional
    public FootballEvent upsertFromSofaEvent(SofascoreEventDto dto, FootballCompetition competition) {
        try {
            FootballEvent event = footballEventRepository
                    .findByExternalSourceAndExternalSourceId(ExternalFootballSource.SOFASCORE, dto.getId())
                    .map(existing -> updateEvent(existing, dto))
                    .orElseGet(() -> createEvent(dto, competition));
            return event;
        } catch (Exception ex) {
            log.error("Error adding/updating event {}", dto.getId(), ex);
            return null;
        }
    }

    private FootballEvent createEvent(SofascoreEventDto dto, FootballCompetition competition) {
        FootballEvent event = new FootballEvent();

        LocalDateTime dateTimeUtc = toUtc(dto.getStartTimestamp());
        event.setDateTime(dateTimeUtc);
        event.setRound(dto.getRoundInfo() != null ? dto.getRoundInfo().getRound() : null);

        event.setCompetition(competition);

        // Resolve or create teams
        FootballTeam home = resolveTeam(dto.getHomeTeam());
        FootballTeam away = resolveTeam(dto.getAwayTeam());
        event.setHomeTeam(home);
        event.setAwayTeam(away);

        // Status + scores
        EventStatus status = mapStatus(dto.getStatus());
        event.setStatus(status);

        if (dto.getHomeScore() != null) event.setHomeScore(dto.getHomeScore().getCurrent());
        if (dto.getAwayScore() != null) event.setAwayScore(dto.getAwayScore().getCurrent());

        // External references
        event.setExternalSource(ExternalFootballSource.SOFASCORE);
        event.setExternalSourceId(dto.getId());
        event.setExternalLink(buildExternalLink(dto));

        event.setLastUpdated(LocalDateTime.now(ZoneOffset.UTC));

        return footballEventRepository.save(event);
    }

    private FootballEvent updateEvent(FootballEvent existing, SofascoreEventDto dto) {
        // If already completed, don't update
        if (existing.getStatus() == EventStatus.COMPLETED) {
            return existing;
        }

        EventStatus newStatus = mapStatus(dto.getStatus());
        existing.setStatus(newStatus);

        if (dto.getHomeScore() != null) existing.setHomeScore(dto.getHomeScore().getCurrent());
        if (dto.getAwayScore() != null) existing.setAwayScore(dto.getAwayScore().getCurrent());

        if (dto.getRoundInfo() != null && dto.getRoundInfo().getRound() != null) {
            existing.setRound(dto.getRoundInfo().getRound());
        }
        if (dto.getStartTimestamp() != null) {
            existing.setDateTime(toUtc(dto.getStartTimestamp()));
        }

        existing.setExternalLink(buildExternalLink(dto));

        existing.setLastUpdated(LocalDateTime.now(ZoneOffset.UTC));
        return footballEventRepository.save(existing);
    }

    private LocalDateTime toUtc(Long epochSeconds) {
        if (epochSeconds == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }

    private FootballTeam resolveTeam(SofascoreTeamDto dto) {
        if (dto == null) throw new IllegalArgumentException("Team payload missing");
        String name = Objects.requireNonNullElse(dto.getName(), "Unknown");
        String shortName = dto.getShortName() != null ? dto.getShortName() : name;
        String country = (dto.getCountry() != null && dto.getCountry().getName() != null)
                ? dto.getCountry().getName()
                : "Unknown";

        // Try by name; if absent create. You can later extend to store SofaScore team id.
        return footballTeamRepository.findByNameOrAlternative(name)
                .orElseGet(() -> footballTeamRepository.save(
                        new FootballTeam(null, name, shortName, country, null, null)
                ));
    }

    private EventStatus mapStatus(SofascoreStatusDto status) {
        if (status == null || status.getType() == null) return EventStatus.SCHEDULED;
        String t = status.getType().toLowerCase();
        switch (t) {
            case "notstarted": return EventStatus.SCHEDULED;
            case "inprogress": return EventStatus.IN_PROGRESS;
            case "finished":   return EventStatus.COMPLETED;
            // Optional: handle "postponed", "canceled", "afterextra", "afterpenalties" if your enum supports them
            default:           return EventStatus.SCHEDULED;
        }
    }

    private String buildExternalLink(SofascoreEventDto dto) {
        if (dto.getSlug() == null || dto.getId() == null) return null;
        // SofaScore event page pattern
        return "https://www.sofascore.com/" + dto.getSlug() + "/" + dto.getId();
    }

    private SofascoreMetadata getCompetitionMetadata(FootballCompetition competition) {
        SofascoreMetadata metadata = competitionMapping.get(competition);
        if (metadata == null) {
            throw new IllegalArgumentException("Unknown competition: " + competition);
        }
        return metadata;
    }
}