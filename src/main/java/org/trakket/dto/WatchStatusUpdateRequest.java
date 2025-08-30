package org.trakket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.trakket.enums.WatchedStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WatchStatusUpdateRequest {

    private Long eventId;
    private WatchedStatus status;

}
