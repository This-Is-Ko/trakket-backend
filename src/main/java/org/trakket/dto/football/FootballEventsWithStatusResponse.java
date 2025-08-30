package org.trakket.dto.football;

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
public class FootballEventsWithStatusResponse extends PaginatedResponse {

    List<FootballEventWithStatus> events;

}
