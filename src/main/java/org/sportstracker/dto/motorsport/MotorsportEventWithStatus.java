package org.sportstracker.dto.motorsport;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.sportstracker.enums.WatchedStatus;

@Data
@AllArgsConstructor
public class MotorsportEventWithStatus {
    private MotorsportEventDto details;
    private WatchedStatus status;
}