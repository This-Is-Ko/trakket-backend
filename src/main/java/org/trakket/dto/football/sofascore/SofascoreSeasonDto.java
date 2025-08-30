package org.trakket.dto.football.sofascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofascoreSeasonDto {
    private Long id;      // e.g. 76986
    private String name;  // "Premier League 25/26"
    private String year;  // "25/26"
}