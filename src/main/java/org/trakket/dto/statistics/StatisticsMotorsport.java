package org.trakket.dto.statistics;

import org.trakket.dto.motorsport.MotorsportEventWithStatus;
import org.trakket.enums.WatchedStatus;

import java.util.List;
import java.util.Map;

public record StatisticsMotorsport(
        Map<WatchedStatus, Long> watchStatusDistribution,
        Map<String, Long> watchedEventsPerCompetition,
        List<MotorsportEventWithStatus> recentEvents,
        Map<Integer, Double> motorsportSeasonCoverage
) {}