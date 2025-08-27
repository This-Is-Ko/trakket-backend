package org.sportstracker.dto.football;

import org.sportstracker.dto.motorsport.MotorsportEventWithStatus;
import org.sportstracker.enums.WatchedStatus;

import java.util.List;
import java.util.Map;

public record StatisticsResponse (
        Map<WatchedStatus, Long> footballWatchStatusDistribution,
        Map<String, Long> footballMatchesPerCompetition,
        List<FootballEventWithStatus> recentMatches,
        Map<WatchedStatus, Long> motorsportWatchStatusDistribution,
        Map<String, Long> motorsportRacesPerCompetition,
        List<MotorsportEventWithStatus> recentRaces
) {}