package org.sportstracker.mapper;

import org.sportstracker.dto.football.FootballEventDto;
import org.sportstracker.model.FootballEvent;

public class FootballEventMapper {
    public static FootballEventDto toDto(FootballEvent entity) {
        return new FootballEventDto(
                entity.getId(),
                entity.getDateTime(),
                entity.getCompetition(),
                entity.getRound(),
                entity.getLocation(),
                entity.getStatus(),
                entity.getExternalLink(),
                entity.getTitle(),
                entity.getSubtitle(),
                entity.getHomeTeam(),
                entity.getAwayTeam(),
                entity.getHomeScore(),
                entity.getAwayScore()
        );
    }
}
