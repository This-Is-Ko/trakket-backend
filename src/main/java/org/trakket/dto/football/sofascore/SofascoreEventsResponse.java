package org.trakket.dto.football.sofascore;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofascoreEventsResponse {
    private List<SofascoreEventDto> events;
}