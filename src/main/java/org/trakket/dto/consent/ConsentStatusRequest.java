package org.trakket.dto.consent;

import jakarta.validation.constraints.NotNull;
import org.trakket.enums.ConsentStatus;

public record ConsentStatusRequest(
    @NotNull ConsentStatus status
) {}
