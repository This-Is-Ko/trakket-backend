package org.sportstracker.dto;

import lombok.Data;
import org.sportstracker.enums.WatchedStatus;

@Data
public class WatchStatusUpdateResponse {
    private Long id;
    private WatchedStatus status;
    private Long eventId;
    private String username;
}
