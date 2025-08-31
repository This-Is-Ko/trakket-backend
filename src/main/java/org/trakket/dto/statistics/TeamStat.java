package org.trakket.dto.statistics;

public record TeamStat(Long teamId, String teamName, Long watchedCount, Double watchedPercent) {}