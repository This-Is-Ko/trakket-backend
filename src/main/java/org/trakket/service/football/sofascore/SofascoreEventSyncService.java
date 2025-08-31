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
import org.trakket.enums.Gender;
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
    private final TeamLogoService teamLogoService;

    private final Map<FootballCompetition, SofascoreMetadata> competitionMapping = Map.ofEntries(
            Map.entry(FootballCompetition.ENGLISH_PREMIER_LEAGUE, new SofascoreMetadata(17, 76986, Gender.M)),
            Map.entry(FootballCompetition.UEFA_CHAMPIONS_LEAGUE, new SofascoreMetadata(7, null, Gender.M)),
            Map.entry(FootballCompetition.LA_LIGA, new SofascoreMetadata(8, 77559, Gender.M)),
            Map.entry(FootballCompetition.SERIE_A, new SofascoreMetadata(23, null, Gender.M)),
            Map.entry(FootballCompetition.BUNDESLIGA, new SofascoreMetadata(35, null, Gender.M)),
            Map.entry(FootballCompetition.ENGLISH_WOMENS_SUPER_LEAGUE, new SofascoreMetadata(1044, 79227, Gender.F))
    );

    public void syncEvents() throws InterruptedException {
        for (FootballCompetition competition : competitionMapping.keySet()) {
            syncRound(competition, null);
            Thread.sleep(10000);
        }
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
                .flatMap(dto -> Mono.fromCallable(() -> upsertFromSofaEvent(dto, competition, metadata))
                        .subscribeOn(Schedulers.boundedElastic()))
                .doOnError(ex -> log.error("SofaScore sync error", ex))
                .subscribe();
    }

    @Transactional
    public FootballEvent upsertFromSofaEvent(SofascoreEventDto dto, FootballCompetition competition, SofascoreMetadata metadata) {
        try {
            FootballEvent event = footballEventRepository
                    .findByExternalSourceAndExternalSourceId(ExternalFootballSource.SOFASCORE, dto.getId())
                    .map(existing -> updateEvent(existing, dto))
                    .orElseGet(() -> createEvent(dto, competition, metadata));
            log.info("Upserted event {}", event.getId());
            return event;
        } catch (Exception ex) {
            log.error("Error adding/updating event {}", dto.getId(), ex);
            return null;
        }
    }

    private FootballEvent createEvent(SofascoreEventDto dto, FootballCompetition competition, SofascoreMetadata metadata) {
        FootballEvent event = new FootballEvent();

        LocalDateTime dateTimeUtc = toUtc(dto.getStartTimestamp());
        event.setDateTime(dateTimeUtc);
        event.setRound(dto.getRoundInfo() != null ? dto.getRoundInfo().getRound() : null);

        event.setCompetition(competition);

        // Resolve or create teams
        FootballTeam home = resolveTeam(dto.getHomeTeam(), metadata);
        FootballTeam away = resolveTeam(dto.getAwayTeam(), metadata);
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

    private FootballTeam resolveTeam(SofascoreTeamDto dto, SofascoreMetadata metadata) {
        if (dto == null) throw new IllegalArgumentException("Team payload missing");

        String name = Objects.requireNonNullElse(dto.getName(), "Unknown");
        String shortName = dto.getShortName() != null ? dto.getShortName() : name;
        String country = (dto.getCountry() != null && dto.getCountry().getName() != null)
                ? dto.getCountry().getName()
                : "Unknown";

        return footballTeamRepository.findBySofascoreExternalId(dto.getId())
                .map(existingTeam -> {
                    // Check if logoUrl is missing
                    if (existingTeam.getLogoUrl() == null || existingTeam.getLogoUrl().isEmpty()) {
                        saveTeamLogo(existingTeam, dto);
                    }
                    return existingTeam;
                })
                .orElseGet(() -> {
                    // Save team without logo first
                    FootballTeam newTeam = new FootballTeam(
                            null,
                            name,
                            shortName,
                            country,
                            null,
                            null,
                            metadata.getGender()
                    );
                    newTeam.setSofascoreExternalId(dto.getId());
                    newTeam = footballTeamRepository.save(newTeam);

                    saveTeamLogo(newTeam, dto);

                    return newTeam;
                });
    }

    private void saveTeamLogo(FootballTeam team, SofascoreTeamDto dto) {
        // Download and upload logo using team ID
        try {
            String logoUrl = teamLogoService.downloadAndUploadLogo(team.getId(), dto.getId());
            team.setLogoUrl(logoUrl);
            team = footballTeamRepository.save(team);
            log.info("Saved logo for team {}: {}", team.getName(), logoUrl);
        } catch (Exception e) {
            log.warn("Failed to download/upload logo for team {}: {}", team.getName(), e.getMessage());
        }
    }

    private EventStatus mapStatus(SofascoreStatusDto status) {
        if (status == null || status.getType() == null) return EventStatus.SCHEDULED;
        String t = status.getType().toLowerCase();
        switch (t) {
            case "notstarted": return EventStatus.SCHEDULED;
            case "inprogress": return EventStatus.IN_PROGRESS;
            case "finished":   return EventStatus.COMPLETED;
            default:           return EventStatus.SCHEDULED;
        }
    }

    private String buildExternalLink(SofascoreEventDto dto) {
        if (dto.getSlug() == null || dto.getId() == null) return null;
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