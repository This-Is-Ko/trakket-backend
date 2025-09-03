package org.trakket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.trakket.dto.football.FootballEventWithStatus;
import org.trakket.dto.motorsport.MotorsportEventWithStatus;
import org.trakket.dto.statistics.CircuitStat;
import org.trakket.dto.statistics.StatisticsFootball;
import org.trakket.dto.statistics.StatisticsMotorsport;
import org.trakket.dto.statistics.StatisticsOverall;
import org.trakket.dto.statistics.StatisticsResponse;
import org.trakket.dto.statistics.TeamStat;
import org.trakket.enums.WatchedStatus;
import org.trakket.mapper.FootballEventMapper;
import org.trakket.mapper.MotorsportEventMapper;
import org.trakket.model.EventWatchStatus;
import org.trakket.model.MotorsportEventWatchStatus;
import org.trakket.model.User;
import org.trakket.repository.CircuitCount;
import org.trakket.repository.FootballCompetitionCount;
import org.trakket.repository.FootballEventRepository;
import org.trakket.repository.FootballEventWatchStatusRepository;
import org.trakket.repository.MotorsportCompetitionCount;
import org.trakket.repository.MotorsportEventRepository;
import org.trakket.repository.MotorsportEventWatchStatusRepository;
import org.trakket.repository.TeamCount;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final FootballEventRepository footballEventRepository;
    private final FootballEventWatchStatusRepository footballEventWatchStatusRepository;
    private final MotorsportEventRepository motorsportEventRepository;
    private final MotorsportEventWatchStatusRepository motorsportEventWatchStatusRepository;

    public StatisticsResponse getStatistics(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // Football statistics
        Map<WatchedStatus, Long> footballStatusDistribution = getWatchStatusDistribution(user, footballEventWatchStatusRepository);
        Map<String, Long> footballMatchesPerCompetition = getFootballMatchesPerCompetition(user);

        List<FootballEventWithStatus> recentMatches = footballEventWatchStatusRepository
                .findByUserAndStatusNotOrderByEvent_DateTimeDesc(user, WatchedStatus.UNWATCHED, PageRequest.of(0, 5))
                .stream()
                .map(ws -> new FootballEventWithStatus(FootballEventMapper.toDto(ws.getEvent()), ws.getStatus()))
                .toList();

        List<TeamStat> topTeams = getTopWatchedTeams(user, 10);
        // example: to compute percentage for a particular team and season:
        // double percent = getTeamSeasonWatchPercentage(user, teamId, seasonString);

        StatisticsFootball footballStatistics = new StatisticsFootball(
                footballStatusDistribution,
                footballMatchesPerCompetition,
                recentMatches,
                topTeams
        );

        // Motorsport statistics
        Map<WatchedStatus, Long> motorsportStatusDistribution = getWatchStatusDistribution(user, motorsportEventWatchStatusRepository);
        Map<String, Long> motorsportRacesPerCompetition = getMotorsportRacesPerCompetition(user);

        List<MotorsportEventWithStatus> recentRaces = motorsportEventWatchStatusRepository
                .findByUserAndStatusNotOrderByEvent_DateTimeDesc(user, WatchedStatus.UNWATCHED, PageRequest.of(0, 5))
                .stream()
                .map(ws -> new MotorsportEventWithStatus(MotorsportEventMapper.toDto(ws.getEvent()), ws.getStatus()))
                .toList();

        Map<Integer, Double> motorsportSeasonCoverage = getMotorsportSeasonCoverage(user);

        StatisticsMotorsport statisticsMotorsport = new StatisticsMotorsport(
                motorsportStatusDistribution,
                motorsportRacesPerCompetition,
                recentRaces,
                motorsportSeasonCoverage
        );

        // Create overall stats
        Map<WatchedStatus, Long> overallWatchStatusDistribution = new EnumMap<>(WatchedStatus.class);
        footballStatusDistribution.forEach((status, count) ->
                overallWatchStatusDistribution.merge(status, count, Long::sum));
        motorsportStatusDistribution.forEach((status, count) ->
                overallWatchStatusDistribution.merge(status, count, Long::sum));
        Map<String, Long> sportsMix = getSportMix(user);
        StatisticsOverall overall = new StatisticsOverall(overallWatchStatusDistribution, sportsMix);


        return new StatisticsResponse(
                overall,
                footballStatistics,
                statisticsMotorsport
        );
    }

    private <T extends EventWatchStatus> Map<WatchedStatus, Long> getWatchStatusDistribution(User user, org.springframework.data.jpa.repository.JpaRepository<T, ?> repository) {
        if (user == null) return Collections.emptyMap();

        Map<WatchedStatus, Long> distribution = repository.findAll().stream()
                .filter(ws -> ws.getUser().equals(user))
                .map(org.trakket.model.EventWatchStatus::getStatus)
                .filter(status -> status != WatchedStatus.UNWATCHED)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        for (WatchedStatus status : WatchedStatus.values()) {
            if (status != WatchedStatus.UNWATCHED) distribution.putIfAbsent(status, 0L);
        }

        return distribution;
    }

    // Share of watched events by sport
    private Map<String, Long> getSportMix(User user) {
        long footballWatched = Optional.ofNullable(footballEventWatchStatusRepository.countWatchedByUser(user, WatchedStatus.UNWATCHED)).orElse(0L);
        long motorsportWatched = Optional.ofNullable(motorsportEventWatchStatusRepository.countWatchedByUser(user, WatchedStatus.UNWATCHED)).orElse(0L);

        Map<String, Long> m = new LinkedHashMap<>();
        m.put("football", footballWatched);
        m.put("motorsport", motorsportWatched);
        return m;
    }

    /**
     * Top N watched teams (combines home + away counts).
     */
    private List<TeamStat> getTopWatchedTeams(User user, int limit) {
        List<TeamCount> home = footballEventWatchStatusRepository.countWatchedHomeTeams(user, WatchedStatus.UNWATCHED);
        List<TeamCount> away = footballEventWatchStatusRepository.countWatchedAwayTeams(user, WatchedStatus.UNWATCHED);

        Map<Long, TeamStatBuilder> agg = new HashMap<>();
        // accumulate home counts
        for (TeamCount tc : home) {
            agg.compute(tc.getTeamId(), (k, b) -> {
                if (b == null) return new TeamStatBuilder(tc.getTeamId(), tc.getTeamName(), tc.getCnt());
                b.count += tc.getCnt();
                return b;
            });
        }
        // accumulate away counts
        for (TeamCount tc : away) {
            agg.compute(tc.getTeamId(), (k, b) -> {
                if (b == null) return new TeamStatBuilder(tc.getTeamId(), tc.getTeamName(), tc.getCnt());
                b.count += tc.getCnt();
                return b;
            });
        }

        long totalWatched = agg.values().stream().mapToLong(b -> b.count).sum();

        return agg.values().stream()
                .map(b -> new TeamStat(b.teamId, b.teamName, b.count, totalWatched == 0 ? 0.0 : (100.0 * b.count / totalWatched)))
                .sorted(Comparator.comparingLong(TeamStat::watchedCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Percentage of a team's matches watched by user in a season.
     * Returns a value in [0..100]. If there are no matches for that team in the season, returns 0.0.
     */
    private double getTeamSeasonWatchPercentage(User user, Long teamId, String season) {
        Long totalMatches = Optional.ofNullable(footballEventRepository.countMatchesForTeamInSeason(teamId)).orElse(0L);
        if (totalMatches == 0) return 0.0;

        Long watched = Optional.ofNullable(footballEventWatchStatusRepository.countWatchedMatchesForTeam(user, teamId, WatchedStatus.UNWATCHED)).orElse(0L);
        return 100.0 * watched / totalMatches;
    }

    /**
     * Top N watched circuits for the user.
     */
    private List<CircuitStat> getTopWatchedCircuits(User user, int limit) {
        List<CircuitCount> counts = motorsportEventWatchStatusRepository.countWatchedByCircuit(user, WatchedStatus.UNWATCHED);
        return counts.stream()
                .map(c -> new CircuitStat(c.getCircuitName(), c.getCnt()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Simple season coverage for motorsport: for each season the user has watched any races,
     * compute watched count and optionally the % of that season that the user watched.
     * This method returns a map: season -> percentage (0..100)
     */
    private Map<Integer, Double> getMotorsportSeasonCoverage(User user) {
        // Fetch watched statuses for the user and group by event.season
        List<MotorsportEventWatchStatus> watchedStatuses = motorsportEventWatchStatusRepository.findAll().stream()
                .filter(ws -> ws.getUser().equals(user) && ws.getStatus() != WatchedStatus.UNWATCHED)
                .toList();

        Map<Integer, Long> watchedPerSeason = watchedStatuses.stream()
                .filter(ws -> ws.getEvent() != null && ws.getEvent().getSeason() != null)
                .collect(Collectors.groupingBy(ws -> ws.getEvent().getSeason(), Collectors.counting()));

        Map<Integer, Double> pctPerSeason = new LinkedHashMap<>();
        for (Map.Entry<Integer, Long> e : watchedPerSeason.entrySet()) {
            Integer season = e.getKey();
            Long watchedCount = e.getValue();
            Long totalRaces = Optional.ofNullable(motorsportEventRepository.countRacesInSeason(season)).orElse(0L);
            double pct = totalRaces == 0 ? 0.0 : (100.0 * watchedCount / totalRaces);
            pctPerSeason.put(season, pct);
        }

        return pctPerSeason;
    }

    // builder to aggregate team totals
    private static class TeamStatBuilder {
        Long teamId;
        String teamName;
        long count;
        TeamStatBuilder(Long teamId, String teamName, long count) {
            this.teamId = teamId;
            this.teamName = teamName;
            this.count = count;
        }
    }

    private Map<String, Long> getFootballMatchesPerCompetition(User user) {
        if (user == null) return Collections.emptyMap();

        return footballEventWatchStatusRepository.countWatchedByCompetition(user, WatchedStatus.UNWATCHED).stream()
                .collect(Collectors.toMap(
                        cc -> cc.getCompetition().getDisplayName(),
                        FootballCompetitionCount::getCnt
                ));
    }

    private Map<String, Long> getMotorsportRacesPerCompetition(User user) {
        if (user == null) return Collections.emptyMap();

        return motorsportEventWatchStatusRepository.countWatchedByCompetition(user, WatchedStatus.UNWATCHED).stream()
                .collect(Collectors.toMap(
                        cc -> cc.getCompetition().getDisplayName(),
                        MotorsportCompetitionCount::getCnt
                ));

        }
}
