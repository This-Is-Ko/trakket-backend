package org.trakket.dto.football.sofascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofascoreTeamDto {
    private Long id;
    private String name;
    private String shortName;
    private SofascoreCountryDto country;
}