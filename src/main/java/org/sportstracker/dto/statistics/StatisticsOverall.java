package org.sportstracker.dto.statistics;

import org.sportstracker.enums.WatchedStatus;

import java.util.Map;

public record StatisticsOverall (
        Map<WatchedStatus, Long> watchStatusDistribution
) {}