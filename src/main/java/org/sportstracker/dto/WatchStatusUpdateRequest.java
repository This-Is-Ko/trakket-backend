package org.sportstracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sportstracker.enums.WatchedStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WatchStatusUpdateRequest {

    private Long eventId;
    private WatchedStatus status;

}
