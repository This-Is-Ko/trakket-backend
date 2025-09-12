package org.trakket.dto.motorsport.thesportsdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SportsDbEventsSeasonResponse {
    private List<SportsDbEventDto> events;
}
