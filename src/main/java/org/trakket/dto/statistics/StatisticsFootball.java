package org.trakket.dto.statistics;

import org.trakket.dto.football.FootballEventWithStatus;
import org.trakket.enums.WatchedStatus;

import java.util.List;
import java.util.Map;

public record StatisticsFootball(
        Map<WatchedStatus, Long> watchStatusDistribution,
        Map<String, Long> perCompetition,
        List<FootballEventWithStatus> recentEvents,
        List<TeamStat> topTeams
) {}