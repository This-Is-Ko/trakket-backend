package org.sportstracker.dto.statistics;

import org.sportstracker.dto.football.FootballEventWithStatus;
import org.sportstracker.enums.WatchedStatus;

import java.util.List;
import java.util.Map;

public record StatisticsFootball(
        Map<WatchedStatus, Long> watchStatusDistribution,
        Map<String, Long> perCompetition,
        List<FootballEventWithStatus> recentEvents
) {}