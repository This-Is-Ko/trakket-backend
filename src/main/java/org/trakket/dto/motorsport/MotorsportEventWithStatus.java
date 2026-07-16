package org.trakket.dto.motorsport;

import org.trakket.enums.WatchedStatus;

public record MotorsportEventWithStatus(
        MotorsportEventDto details,
        WatchedStatus status
) {}
