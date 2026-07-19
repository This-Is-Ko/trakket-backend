package org.trakket.dto.admin;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record MotorsportEventUpdateRequest(
        @NotNull LocalDateTime dateTime,
        String winner
) {}
