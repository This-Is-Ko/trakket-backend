package org.trakket.dto.motorsport;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.trakket.enums.WatchedStatus;

@Data
@AllArgsConstructor
public class MotorsportEventWithStatus {
    private MotorsportEventDto details;
    private WatchedStatus status;
}