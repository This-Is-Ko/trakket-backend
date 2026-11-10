package org.trakket.dto.motorsport;

import org.trakket.enums.EventStatus;
import org.trakket.enums.MotorsportCompetition;

import java.time.LocalDateTime;

public record MotorsportEventDto(
        Long id,
        LocalDateTime dateTime,
        MotorsportCompetition competition,
        Integer season,
        Integer round,
        String location,
        EventStatus status,
        String externalLink,
        String title,
        String subtitle,
        String raceName,
        String circuitName,
        String winner,
        String flagUrl
) {}
