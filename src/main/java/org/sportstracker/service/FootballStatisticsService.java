package org.sportstracker.service;

import lombok.RequiredArgsConstructor;
import org.sportstracker.dto.FootballEventDto;
import org.sportstracker.dto.FootballEventWithStatus;
import org.sportstracker.dto.FootballStatisticsResponse;
import org.sportstracker.dto.RecentMatchDto;
import org.sportstracker.enums.WatchedStatus;
import org.sportstracker.mapper.FootballEventMapper;
import org.sportstracker.model.User;
import org.sportstracker.model.FootballEvent;
import org.sportstracker.model.FootballEventWatchStatus;
import org.sportstracker.repository.FootballEventRepository;
import org.sportstracker.repository.FootballEventWatchStatusRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FootballStatisticsService {

    private final FootballEventRepository footballEventRepository;
    private final FootballEventWatchStatusRepository watchStatusRepository;

    public FootballStatisticsResponse getStatistics(User user) {
        // All statistics below exclude UNWATCHED status

        // Watch status distribution
        Map<WatchedStatus, Long> watchStatusDistribution = watchStatusRepository
                .findAllByUser(user).stream()
                .map(FootballEventWatchStatus::getStatus)
                .filter(status -> status != WatchedStatus.UNWATCHED) // skip UNWATCHED
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        // Ensure all other enum values are present
        for (WatchedStatus status : WatchedStatus.values()) {
            if (status != WatchedStatus.UNWATCHED) {
                watchStatusDistribution.putIfAbsent(status, 0L);
            }
        }

        // Matches per competition
        Map<String, Long> matchesPerCompetition =
                (user == null)
                        ? Collections.emptyMap()
                        : watchStatusRepository.countWatchedByCompetition(user, WatchedStatus.UNWATCHED)
                        .stream()
                        .collect(Collectors.toMap(
                                FootballEventWatchStatusRepository.CompetitionCount::getCompetition,
                                FootballEventWatchStatusRepository.CompetitionCount::getCnt
                        ));

        // Most recent matches which have status other than UNWATCHED status
        List<FootballEventWithStatus> recentMatches = watchStatusRepository
                .findByUserAndStatusNotOrderByEvent_DateTimeDesc(
                        user,
                        WatchedStatus.UNWATCHED,
                        PageRequest.of(0, 10)
                )
                .stream()
                .map(ws -> new FootballEventWithStatus(FootballEventMapper.toDto(ws.getEvent()), ws.getStatus()))
                .toList();

        return new FootballStatisticsResponse(watchStatusDistribution, matchesPerCompetition, recentMatches);
    }
}
