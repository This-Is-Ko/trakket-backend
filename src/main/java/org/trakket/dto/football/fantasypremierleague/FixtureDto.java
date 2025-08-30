package org.trakket.dto.football.fantasypremierleague;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FixtureDto {
    private Long code;

    private Integer event;

    private boolean finished;

    @JsonProperty("kickoff_time")
    private LocalDateTime kickoffTime;

    private boolean started;

    @JsonProperty("team_a")
    private Integer teamA;

    @JsonProperty("team_h")
    private Integer teamH;

    @JsonProperty("team_a_score")
    private Integer teamAScore;

    @JsonProperty("team_h_score")
    private Integer teamHScore;
}
