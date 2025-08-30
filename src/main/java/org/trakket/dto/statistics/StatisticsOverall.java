package org.trakket.dto.statistics;

import org.trakket.enums.WatchedStatus;

import java.util.Map;

public record StatisticsOverall (
        Map<WatchedStatus, Long> watchStatusDistribution
) {}