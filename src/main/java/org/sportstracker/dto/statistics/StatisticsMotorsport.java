package org.sportstracker.dto.statistics;

import org.sportstracker.dto.football.FootballEventWithStatus;
import org.sportstracker.dto.motorsport.MotorsportEventWithStatus;
import org.sportstracker.enums.WatchedStatus;

import java.util.List;
import java.util.Map;

public record StatisticsMotorsport(
        Map<WatchedStatus, Long> watchStatusDistribution,
        Map<String, Long> watchedEventsPerCompetition,
        List<MotorsportEventWithStatus> recentEvents
) {}