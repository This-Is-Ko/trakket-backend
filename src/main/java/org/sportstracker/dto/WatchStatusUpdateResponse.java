package org.sportstracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sportstracker.enums.WatchedStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WatchStatusUpdateResponse {

    private Long id;
    private WatchedStatus status;
    private Long eventId;
    private String username;

}
