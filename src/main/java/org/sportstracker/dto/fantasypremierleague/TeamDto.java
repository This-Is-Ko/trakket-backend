package org.sportstracker.dto.fantasypremierleague;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TeamDto {
    private Integer id;

    private String name;

    @JsonProperty("short_name")
    private String shortName;

}
