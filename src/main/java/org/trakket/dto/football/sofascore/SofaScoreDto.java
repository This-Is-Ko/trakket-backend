package org.trakket.dto.football.sofascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofaScoreDto {
    private Integer current;     // live/final score
}