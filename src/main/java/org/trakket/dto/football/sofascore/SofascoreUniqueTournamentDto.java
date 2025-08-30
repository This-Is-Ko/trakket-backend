package org.trakket.dto.football.sofascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofascoreUniqueTournamentDto {
    private Integer id;     // e.g. 17 (Premier League)
    private String name;
    private String slug;
}