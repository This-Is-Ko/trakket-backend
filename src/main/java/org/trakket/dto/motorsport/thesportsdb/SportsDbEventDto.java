package org.trakket.dto.motorsport.thesportsdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SportsDbEventDto {

    @JsonProperty("idEvent")
    private String idEvent;

    @JsonProperty("strEvent")
    private String strEvent;

    @JsonProperty("idLeague")
    private String idLeague;

    @JsonProperty("strLeague")
    private String strLeague;

    @JsonProperty("strLeagueBadge")
    private String strLeagueBadge;

    @JsonProperty("strSeason")
    private String strSeason;

    @JsonProperty("intRound")
    private String intRound;

    @JsonProperty("strVenue")
    private String strVenue;

    @JsonProperty("strResult")
    private String strResult;

    @JsonProperty("strStatus")
    private String strStatus;

    @JsonProperty("strTimestamp")
    private String strTimestamp;
}
