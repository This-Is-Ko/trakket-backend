package org.trakket.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.trakket.enums.EventStatus;
import org.trakket.enums.FootballCompetition;

import java.time.LocalDateTime;

public record FootballEventUpdateRequest(
        @NotNull LocalDateTime dateTime,
        @NotNull FootballCompetition competition,
        @NotNull EventStatus status,
        @PositiveOrZero Integer round,
        @Size(max = 200) String location,
        @Size(max = 255) String externalLink,
        @Min(0) @Max(99) Integer homeScore,
        @Min(0) @Max(99) Integer awayScore
) {}
