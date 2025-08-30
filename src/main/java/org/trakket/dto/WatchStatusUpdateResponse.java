package org.trakket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.trakket.enums.WatchedStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WatchStatusUpdateResponse {

    private Long id;
    private WatchedStatus status;
    private Long eventId;
    private String username;

}
