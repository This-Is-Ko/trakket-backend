package org.sportstracker.dto;

import org.sportstracker.enums.WatchedStatus;

import java.util.List;
import java.util.Map;

public record FootballStatisticsResponse(
        Map<WatchedStatus, Long> watchStatusDistribution,
        Map<String, Long> matchesPerCompetition,
        List<FootballEventWithStatus> recentMatches
) {}