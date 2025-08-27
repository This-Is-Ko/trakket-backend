package org.sportstracker.dto.football;

import org.sportstracker.enums.EventStatus;
import org.sportstracker.enums.FootballCompetition;

import java.time.LocalDateTime;

public record FootballEventDto (
        Long id,
        LocalDateTime dateTime,
        FootballCompetition competition,
        Integer round,
        String location,
        EventStatus status,
        String externalLink,
        String title,
        String subtitle,
        String homeTeam,
        String awayTeam,
        Integer homeScore,
        Integer awayScore
) {}
