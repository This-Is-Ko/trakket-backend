package org.trakket.dto.football.sofascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofascoreEventDto {
    private Long id;                       // SofaScore event id
    private String slug;                   // for external link
    private Long startTimestamp;           // epoch seconds
    private SofascoreStatusDto status;          // finished / inprogress / notstarted
    private SofascoreRoundInfoDto roundInfo;    // round number
    private SofascoreTeamDto homeTeam;
    private SofascoreTeamDto awayTeam;
    private SofaScoreDto homeScore;        // may be null before start
    private SofaScoreDto awayScore;        // may be null before start
    private SofascoreUniqueTournamentDto uniqueTournament; // contains tournament id
    private SofascoreSeasonDto season;          // contains season id
}

