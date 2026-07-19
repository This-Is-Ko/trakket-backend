package org.trakket.dto.consent;

import org.trakket.enums.ConsentStatus;

import java.time.LocalDateTime;

public record ConsentStatusResponse(
    ConsentStatus status,
    LocalDateTime updatedAt,
    int policyVersion
) {}
