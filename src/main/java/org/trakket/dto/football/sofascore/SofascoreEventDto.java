package org.trakket.dto.football.sofascore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SofascoreEventDto {
    private Long id;
    private String slug;
    private Long startTimestamp;
    private SofascoreStatusDto status;
    private SofascoreRoundInfoDto roundInfo;
    private SofascoreTeamDto homeTeam;
    private SofascoreTeamDto awayTeam;
    private SofaScoreDto homeScore;
    private SofaScoreDto awayScore;
    private SofascoreUniqueTournamentDto uniqueTournament;
    private SofascoreSeasonDto season;
}

