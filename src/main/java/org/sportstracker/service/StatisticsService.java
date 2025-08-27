package org.sportstracker.service;

import lombok.RequiredArgsConstructor;
import org.sportstracker.dto.football.FootballEventWithStatus;
import org.sportstracker.dto.football.StatisticsResponse;
import org.sportstracker.dto.motorsport.MotorsportEventWithStatus;
import org.sportstracker.enums.WatchedStatus;
import org.sportstracker.mapper.FootballEventMapper;
import org.sportstracker.mapper.MotorsportEventMapper;
import org.sportstracker.model.EventWatchStatus;
import org.sportstracker.model.User;
import org.sportstracker.repository.CompetitionCount;
import org.sportstracker.repository.FootballEventRepository;
import org.sportstracker.repository.FootballEventWatchStatusRepository;
import org.sportstracker.repository.MotorsportEventRepository;
import org.sportstracker.repository.MotorsportEventWatchStatusRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
                .findByUserAndStatusNotOrderByEvent_DateTimeDesc(user, WatchedStatus.UNWATCHED, PageRequest.of(0, 10))
                .stream()
                .map(ws -> new FootballEventWithStatus(FootballEventMapper.toDto(ws.getEvent()), ws.getStatus()))
                .toList();

        // --- Motorsport statistics ---
        Map<WatchedStatus, Long> motorsportStatusDistribution = getWatchStatusDistribution(user, motorsportWatchStatusRepository);
        Map<String, Long> motorsportRacesPerCompetition = getMotorsportRacesPerCompetition(user);

        List<MotorsportEventWithStatus> recentRaces = motorsportWatchStatusRepository
                .findByUserAndStatusNotOrderByEvent_DateTimeDesc(user, WatchedStatus.UNWATCHED, PageRequest.of(0, 10))
                .stream()
                .map(ws -> new MotorsportEventWithStatus(MotorsportEventMapper.toDto(ws.getEvent()), ws.getStatus()))
                .toList();

        return new StatisticsResponse(
                footballStatusDistribution,
                footballMatchesPerCompetition,
                recentMatches,
                motorsportStatusDistribution,
                motorsportRacesPerCompetition,
                recentRaces
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
