package org.trakket.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.trakket.enums.EventStatus;
import org.trakket.enums.MotorsportCompetition;

import java.time.LocalDateTime;

public record MotorsportEventUpdateRequest(
        @NotNull LocalDateTime dateTime,
        @NotNull MotorsportCompetition competition,
        @NotNull EventStatus status,
        @PositiveOrZero Integer round,
        @Positive Integer season,
        @Size(max = 200) String location,
        @Size(max = 255) String externalLink,
        @NotBlank @Size(max = 100) String raceName,
        @Size(max = 100) String circuitName,
        @Size(max = 100) String winner,
        @Size(max = 200) String flagUrl
) {}
