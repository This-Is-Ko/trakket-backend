package org.trakket.dto.football.sofascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofascoreStatusDto {
    private Integer code;      // e.g. 100 for ended
    private String description; // "Ended"
    private String type;       // "finished" | "inprogress" | "notstarted" | others
}