package org.trakket.dto.football.sofascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofascoreScoreDto {
    private Integer current;
}