package org.trakket.mapper;

import org.trakket.dto.motorsport.MotorsportEventDto;
import org.trakket.model.MotorsportEvent;

public class MotorsportEventMapper {

    public static MotorsportEventDto toDto(MotorsportEvent event) {
        return new MotorsportEventDto(
                event.getId(),
                event.getDateTime(),
                event.getCompetition(),
                event.getSeason(),
                event.getRound(),
                event.getLocation(),
                event.getStatus(),
                event.getExternalLink(),
                event.getTitle(),
                event.getSubtitle(),
                event.getRaceName(),
                event.getCircuitName(),
                event.getWinner(),
                event.getFlagUrl()
        );
    }
}
