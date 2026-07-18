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
import org.trakket.enums.MotorsportCompetition;
import org.trakket.enums.WatchedStatus;
import org.trakket.mapper.FootballEventMapper;
import org.trakket.mapper.MotorsportEventMapper;
import org.trakket.model.FootballEventWatchStatus;
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
        Map<WatchedStatus, Long> footballStatusDistribution = getFootballStatusDistribution(user);
        Map<String, Long> footballMatchesPerCompetition = getFootballMatchesPerCompetition(user);

        List<FootballEventWithStatus> recentMatches = footballEventWatchStatusRepository
                .findByUserAndStatusNotOrderByEvent_DateTimeDesc(user, WatchedStatus.UNWATCHED, PageRequest.of(0, 5))
                .stream()
                .map(ws -> new FootballEventWithStatus(FootballEventMapper.toDto(ws.getEvent()), ws.getStatus()))
                .toList();

        List<TeamStat> topTeams = getTopWatchedTeams(user, 10);

        Map<String, Map<Integer, Double>> teamSeasonCoverage = getTeamSeasonCoverage(user, topTeams);

        StatisticsFootball footballStatistics = new StatisticsFootball(
                footballStatusDistribution,
                footballMatchesPerCompetition,
                recentMatches,
                topTeams,
                teamSeasonCoverage
        );

        // Motorsport statistics
        Map<WatchedStatus, Long> motorsportStatusDistribution = getMotorsportStatusDistribution(user);
        Map<String, Long> motorsportRacesPerCompetition = getMotorsportRacesPerCompetition(user);

        List<MotorsportEventWithStatus> recentRaces = motorsportEventWatchStatusRepository
                .findByUserAndStatusNotOrderByEvent_DateTimeDesc(user, WatchedStatus.UNWATCHED, PageRequest.of(0, 5))
                .stream()
                .map(ws -> new MotorsportEventWithStatus(MotorsportEventMapper.toDto(ws.getEvent()), ws.getStatus()))
                .toList();

        Map<String, Map<Integer, Double>> motorsportSeasonCoverage = getMotorsportSeasonCoverage(user);

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

    private Map<WatchedStatus, Long> getFootballStatusDistribution(User user) {
        Map<WatchedStatus, Long> distribution = footballEventWatchStatusRepository.findAllByUser(user).stream()
                .map(FootballEventWatchStatus::getStatus)
                .filter(status -> status != WatchedStatus.UNWATCHED)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        for (WatchedStatus status : WatchedStatus.values()) {
            if (status != WatchedStatus.UNWATCHED) distribution.putIfAbsent(status, 0L);
        }

        return distribution;
    }

    private Map<WatchedStatus, Long> getMotorsportStatusDistribution(User user) {
        Map<WatchedStatus, Long> distribution = motorsportEventWatchStatusRepository.findAllByUser(user).stream()
                .map(MotorsportEventWatchStatus::getStatus)
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
    private Map<String, Map<Integer, Double>> getTeamSeasonCoverage(User user, List<TeamStat> topTeams) {
        Map<String, Map<Integer, Double>> result = new LinkedHashMap<>();
        for (TeamStat team : topTeams) {
            Long teamId = team.teamId();
            List<Integer> seasons = footballEventRepository.getSeasonsForTeam(teamId);
            Map<Integer, Double> seasonCoverage = new LinkedHashMap<>();
            for (int season : seasons) {
                Long totalMatches = Optional.ofNullable(
                        footballEventRepository.countMatchesForTeamInYear(teamId, season)
                ).orElse(0L);
                if (totalMatches == 0) continue;
                Long watchedMatches = Optional.ofNullable(
                        footballEventWatchStatusRepository.countWatchedMatchesForTeamInYear(
                                user, teamId, WatchedStatus.UNWATCHED, season)
                ).orElse(0L);
                double pct = 100.0 * watchedMatches / totalMatches;
                seasonCoverage.put(season, pct);
            }
            if (!seasonCoverage.isEmpty()) {
                result.put(team.teamName(), seasonCoverage);
            }
        }
        return result;
    }

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
    private Map<String, Map<Integer, Double>> getMotorsportSeasonCoverage(User user) {
        List<MotorsportEventWatchStatus> watchedStatuses = motorsportEventWatchStatusRepository.findAllByUser(user).stream()
                .filter(ws -> ws.getStatus() != WatchedStatus.UNWATCHED)
                .filter(ws -> ws.getEvent() != null && ws.getEvent().getSeason() != null && ws.getEvent().getCompetition() != null)
                .toList();

        // Group by competition, then season
        Map<MotorsportCompetition, Map<Integer, Long>> raw = watchedStatuses.stream()
                .collect(Collectors.groupingBy(
                        ws -> ws.getEvent().getCompetition(),
                        Collectors.groupingBy(ws -> ws.getEvent().getSeason(), Collectors.counting())
                ));

        Map<String, Map<Integer, Double>> result = new LinkedHashMap<>();
        for (Map.Entry<MotorsportCompetition, Map<Integer, Long>> compEntry : raw.entrySet()) {
            String compName = compEntry.getKey().getDisplayName();
            Map<Integer, Double> pctPerSeason = new LinkedHashMap<>();

            for (Map.Entry<Integer, Long> seasonEntry : compEntry.getValue().entrySet()) {
                Integer season = seasonEntry.getKey();
                Long watchedCount = seasonEntry.getValue();
                Long totalRaces = Optional.ofNullable(
                        motorsportEventRepository.countRacesInSeasonForCompetition(season, compEntry.getKey())
                ).orElse(0L);
                double pct = totalRaces == 0 ? 0.0 : (100.0 * watchedCount / totalRaces);
                pctPerSeason.put(season, pct);
            }

            result.put(compName, pctPerSeason);
        }

        return result;
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
