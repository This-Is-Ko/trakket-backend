package org.trakket.dto.football.sofascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofascoreTeamDto {
    private Long id;              // SofaScore team id (not stored, used for matching if desired)
    private String name;          // "West Ham United"
    private String shortName;     // "West Ham"
    private SofascoreCountryDto country;
}