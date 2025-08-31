package org.trakket.mapper;

import org.trakket.dto.motorsport.jolpicaf1.RacesResponseDto;
import org.trakket.enums.EventStatus;
import org.trakket.enums.ExternalMotorsportSource;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.model.MotorsportEvent;
import org.trakket.dto.motorsport.MotorsportEventDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MotorsportEventMapper {

    public static MotorsportEventDto toDto(MotorsportEvent event) {
        return new MotorsportEventDto(
                event.getId(),
                event.getDateTime(),
                event.getCompetition(),
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
