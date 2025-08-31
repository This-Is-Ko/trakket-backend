package org.trakket.dto.football;

import org.trakket.enums.EventStatus;
import org.trakket.enums.FootballCompetition;

import java.time.LocalDateTime;

public record FootballEventDto(
        Long id,
        LocalDateTime dateTime,
        FootballCompetition competition,
        Integer round,
        String location,
        EventStatus status,
        String externalLink,
        String title,
        String subtitle,
        Long homeTeamId,
        String homeTeamName,
        String homeTeamLogoUrl,
        Integer homeScore,
        Long awayTeamId,
        String awayTeamName,
        String awayTeamLogoUrl,
        Integer awayScore
) {}