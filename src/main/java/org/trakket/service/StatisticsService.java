package org.trakket.service;

import lombok.RequiredArgsConstructor;
import org.trakket.dto.football.FootballEventWithStatus;
import org.trakket.dto.statistics.StatisticsFootball;
import org.trakket.dto.statistics.StatisticsMotorsport;
import org.trakket.dto.statistics.StatisticsOverall;
import org.trakket.dto.statistics.StatisticsResponse;
import org.trakket.dto.motorsport.MotorsportEventWithStatus;
import org.trakket.enums.WatchedStatus;
import org.trakket.mapper.FootballEventMapper;
import org.trakket.mapper.MotorsportEventMapper;
import org.trakket.model.EventWatchStatus;
import org.trakket.model.User;
import org.trakket.repository.CompetitionCount;
import org.trakket.repository.FootballEventRepository;
import org.trakket.repository.FootballEventWatchStatusRepository;
import org.trakket.repository.MotorsportEventRepository;
import org.trakket.repository.MotorsportEventWatchStatusRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final FootballEventRepository footballEventRepository;
    private final FootballEventWatchStatusRepository footballWatchStatusRepository;
    private final MotorsportEventRepository motorsportEventRepository;
    private final MotorsportEventWatchStatusRepository motorsportWatchStatusRepository;

    public StatisticsResponse getStatistics(User user) {
        // --- Football statistics ---
        Map<WatchedStatus, Long> footballStatusDistribution = getWatchStatusDistribution(user, footballWatchStatusRepository);
        Map<String, Long> footballMatchesPerCompetition = getFootballMatchesPerCompetition(user);

        List<FootballEventWithStatus> recentMatches = footballWatchStatusRepository
                .findByUserAndStatusNotOrderByEvent_DateTimeDesc(user, WatchedStatus.UNWATCHED, PageRequest.of(0, 5))
                .stream()
                .map(ws -> new FootballEventWithStatus(FootballEventMapper.toDto(ws.getEvent()), ws.getStatus()))
                .toList();
        StatisticsFootball footballStatistics = new StatisticsFootball(footballStatusDistribution, footballMatchesPerCompetition, recentMatches);

        // --- Motorsport statistics ---
        Map<WatchedStatus, Long> motorsportStatusDistribution = getWatchStatusDistribution(user, motorsportWatchStatusRepository);
        Map<String, Long> motorsportRacesPerCompetition = getMotorsportRacesPerCompetition(user);

        List<MotorsportEventWithStatus> recentRaces = motorsportWatchStatusRepository
                .findByUserAndStatusNotOrderByEvent_DateTimeDesc(user, WatchedStatus.UNWATCHED, PageRequest.of(0, 5))
                .stream()
                .map(ws -> new MotorsportEventWithStatus(MotorsportEventMapper.toDto(ws.getEvent()), ws.getStatus()))
                .toList();
        StatisticsMotorsport statisticsMotorsport = new StatisticsMotorsport(motorsportStatusDistribution, motorsportRacesPerCompetition, recentRaces);

        // Create overall stats
        Map<WatchedStatus, Long> overallWatchStatusDistribution = new EnumMap<>(WatchedStatus.class);
        footballStatusDistribution.forEach((status, count) ->
                overallWatchStatusDistribution.merge(status, count, Long::sum));
        motorsportStatusDistribution.forEach((status, count) ->
                overallWatchStatusDistribution.merge(status, count, Long::sum));
        StatisticsOverall overall = new StatisticsOverall(overallWatchStatusDistribution);
        return new StatisticsResponse(
                overall,
                footballStatistics,
                statisticsMotorsport
        );
    }

    private <T extends EventWatchStatus> Map<WatchedStatus, Long> getWatchStatusDistribution(User user, JpaRepository<T, ?> repository) {
        if (user == null) return Collections.emptyMap();

        Map<WatchedStatus, Long> distribution = repository.findAll().stream()
                .filter(ws -> ws.getUser().equals(user))
                .map(EventWatchStatus::getStatus)
                .filter(status -> status != WatchedStatus.UNWATCHED)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        for (WatchedStatus status : WatchedStatus.values()) {
            if (status != WatchedStatus.UNWATCHED) distribution.putIfAbsent(status, 0L);
        }

        return distribution;
    }

    private Map<String, Long> getFootballMatchesPerCompetition(User user) {
        if (user == null) return Collections.emptyMap();

        return footballWatchStatusRepository.countWatchedByCompetition(user, WatchedStatus.UNWATCHED).stream()
                .collect(Collectors.toMap(
                        CompetitionCount::getCompetition,
                        CompetitionCount::getCnt
                ));
    }

    private Map<String, Long> getMotorsportRacesPerCompetition(User user) {
        if (user == null) return Collections.emptyMap();

        return motorsportWatchStatusRepository.countWatchedByCompetition(user, WatchedStatus.UNWATCHED).stream()
                .collect(Collectors.toMap(
                        CompetitionCount::getCompetition,
                        CompetitionCount::getCnt
                ));
    }

}
