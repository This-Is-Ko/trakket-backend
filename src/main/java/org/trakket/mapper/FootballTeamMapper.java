package org.trakket.mapper;

import org.trakket.dto.football.FootballTeamDto;
import org.trakket.model.FootballTeam;

public class FootballTeamMapper {
    public static FootballTeamDto toDto(FootballTeam team) {
        if (team == null) {
            return null;
        }
        return new FootballTeamDto(
                team.getId(),
                team.getName(),
                team.getShortName(),
                team.getCountry(),
                team.getLogoUrl(),
                team.getAlternativeNames(),
                team.getGender()
        );
    }
}


