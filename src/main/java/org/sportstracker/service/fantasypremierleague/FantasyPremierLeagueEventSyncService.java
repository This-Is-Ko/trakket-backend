package org.sportstracker.service.fantasypremierleague;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sportstracker.dto.fantasypremierleague.FixtureDto;
import org.sportstracker.enums.EventStatus;
import org.sportstracker.enums.ExternalFootballSource;
import org.sportstracker.model.FootballEvent;
import org.sportstracker.repository.FootballEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class FantasyPremierLeagueEventSyncService {

    private final FootballEventRepository footballEventRepository;
    private final FantasyPremierLeagueClient apiClient;

    // From https://fantasy.premierleague.com/api/bootstrap-static/
    private static final Map<Integer, String> TEAM_ID_TO_NAME = Map.ofEntries(
            Map.entry(1, "Arsenal"),
            Map.entry(2, "Aston Villa"),
            Map.entry(3, "Burnley"),
            Map.entry(4, "Bournemouth"),
            Map.entry(5, "Brentford"),
            Map.entry(6, "Brighton"),
            Map.entry(7, "Chelsea"),
            Map.entry(8, "Crystal Palace"),
            Map.entry(9, "Everton"),
            Map.entry(10, "Fulham"),
            Map.entry(11, "Leeds"),
            Map.entry(12, "Liverpool"),
            Map.entry(13, "Man City"),
            Map.entry(14, "Man Utd"),
            Map.entry(15, "Newcastle"),
            Map.entry(16, "Nott'm Forest"),
            Map.entry(17, "Sunderland"),
            Map.entry(18, "Spurs"),
            Map.entry(19, "West Ham"),
            Map.entry(20, "Wolves")
    );

    public void syncEvents() {
        log.info("Syncing EPL events with fantasy premier league");
        apiClient.fetchFixtures()
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::upsertFromFixture)
                .subscribe(
                        event -> log.info("Processed fixture: {}", event.getId()),
                        error -> log.error("Error syncing fixtures", error)
                );
    }

    @Transactional
    public Mono<FootballEvent> upsertFromFixture(FixtureDto dto) {
        return Mono.fromCallable(() ->
                footballEventRepository
                        .findByExternalSourceAndExternalSourceId(
                                ExternalFootballSource.FANTASY_PREMIER_LEAGUE,
                                dto.getCode()
                        )
                        .map(existing -> updateEvent(existing, dto))
                        .orElseGet(() -> createEvent(dto))
        );
    }

    private FootballEvent createEvent(FixtureDto dto) {
        FootballEvent event = new FootballEvent();
        event.setDateTime(dto.getKickoffTime());
        event.setRound(dto.getEvent());
        event.setHomeTeam(getTeamName(dto.getTeamH()));
        event.setAwayTeam(getTeamName(dto.getTeamA()));
        event.setCompetition("English Premier League");

        // external references
        event.setExternalSource(ExternalFootballSource.FANTASY_PREMIER_LEAGUE);
        event.setExternalSourceId(dto.getCode());

        EventStatus newEvenStatus = determineEventStatus(dto);
        event.setStatus(newEvenStatus);

        // initial scores
        if (dto.isStarted()) {
            event.setHomeScore(dto.getTeamHScore());
            event.setAwayScore(dto.getTeamAScore());
        }

        event.setLastUpdated(LocalDateTime.now());
        return footballEventRepository.save(event);
    }

    private FootballEvent updateEvent(FootballEvent existing, FixtureDto dto) {
        if (existing.getStatus() == EventStatus.COMPLETED) {
            return existing;
        }

        EventStatus newEvenStatus = determineEventStatus(dto);
        if (existing.getStatus() == newEvenStatus) {
            return existing;
        } else {
            existing.setStatus(newEvenStatus);
        }

        if (dto.isStarted()) {
            existing.setHomeScore(dto.getTeamHScore());
            existing.setAwayScore(dto.getTeamAScore());
        }

        existing.setLastUpdated(LocalDateTime.now());
        return footballEventRepository.save(existing);
    }

    private String getTeamName(Integer teamId) {
        return TEAM_ID_TO_NAME.getOrDefault(teamId, "Unknown");
    }

    private EventStatus determineEventStatus(FixtureDto dto) {
        if (!dto.isFinished() && !dto.isStarted()) {
            return EventStatus.SCHEDULED;
        } else if (dto.isStarted() && !dto.isFinished()) {
            return EventStatus.IN_PROGRESS;
        } else {
            return EventStatus.COMPLETED;
        }
    }

}
