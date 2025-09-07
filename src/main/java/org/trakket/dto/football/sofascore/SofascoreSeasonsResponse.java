package org.trakket.dto.football.sofascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SofascoreSeasonsResponse {
    private List<SofascoreSeasonDto> seasons;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class SofascoreSeasonDto {
        private String year;
        private Integer id;
    }
}

