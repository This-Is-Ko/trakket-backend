package org.trakket.dto.motorsport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.trakket.dto.PaginatedResponse;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MotorsportEventsWithStatusResponse extends PaginatedResponse {

    private List<MotorsportEventWithStatus> events;

}
