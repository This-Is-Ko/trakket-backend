package org.trakket.dto.admin;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record FootballEventUpdateRequest(
        @NotNull LocalDateTime dateTime,
        Integer homeScore,
        Integer awayScore
) {}
