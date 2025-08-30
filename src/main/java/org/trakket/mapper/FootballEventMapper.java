package org.trakket.mapper;

import org.trakket.dto.football.FootballEventDto;
import org.trakket.model.FootballEvent;

public class FootballEventMapper {
    public static FootballEventDto toDto(FootballEvent event) {
        return new FootballEventDto(
                event.getId(),
                event.getDateTime(),
                event.getCompetition(),
                event.getRound(),
                event.getLocation(),
                event.getStatus(),
                event.getExternalLink(),
                event.getTitle(),
                event.getSubtitle(),
                event.getHomeTeam().getId(),
                event.getHomeTeam().getName(),
                event.getAwayTeam().getId(),
                event.getAwayTeam().getName(),
                event.getHomeScore(),
                event.getAwayScore()
        );
    }
}
