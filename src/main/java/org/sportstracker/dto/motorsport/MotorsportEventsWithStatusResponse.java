package org.sportstracker.dto.motorsport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.sportstracker.dto.PaginatedResponse;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MotorsportEventsWithStatusResponse extends PaginatedResponse {

    private List<MotorsportEventWithStatus> events;

}
