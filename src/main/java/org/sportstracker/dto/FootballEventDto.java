package org.sportstracker.dto;

import org.sportstracker.enums.EventStatus;

import java.time.LocalDateTime;

public record FootballEventDto (
        Long id,
        LocalDateTime dateTime,
        String competition,
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
