package org.sportstracker.dto;

import lombok.Data;
import org.sportstracker.enums.WatchedStatus;

@Data
public class WatchStatusUpdateRequest {
    private Long eventId;
    private WatchedStatus status;
}
