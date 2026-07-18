package org.trakket.dto.landing;

import org.trakket.enums.EventStatus;

import java.time.LocalDateTime;

public record LandingEventDto(
        Long id,
        String sportType,
        String competition,
        String competitionLogoUrl,
        String title,
        String subtitle,
        LocalDateTime dateTime,
        EventStatus status,
        String location,
        // Football-specific (nullable)
        String homeTeamName,
        String homeTeamLogoUrl,
        Integer homeScore,
        String awayTeamName,
        String awayTeamLogoUrl,
        Integer awayScore,
        // Motorsport-specific (nullable)
        String raceName,
        String circuitName,
        String winner,
        String flagUrl
) {}
