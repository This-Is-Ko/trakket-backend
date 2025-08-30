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

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

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
                event.getWinner()
        );
    }

    public static MotorsportEvent toEntity(RacesResponseDto.Race race, boolean includeWinner) {
        MotorsportEvent event = new MotorsportEvent();

        event.setCompetition(MotorsportCompetition.FORMULA_ONE);
        event.setStatus(EventStatus.SCHEDULED);
        event.setExternalSource(ExternalMotorsportSource.JOLPICA_F1);
        event.setLastUpdated(LocalDateTime.now());
        event.setSeason(Integer.parseInt(race.getSeason()));
        event.setRound(Integer.parseInt(race.getRound()));
        event.setRaceName(race.getRaceName());
        event.setCircuitName(race.getCircuit().getCircuitName());

        if (race.getCircuit() != null && race.getCircuit().getLocation() != null) {
            String loc = race.getCircuit().getLocation().getLocality() + ", " +
                    race.getCircuit().getLocation().getCountry();
            event.setLocation(loc);
        }

        event.setExternalLink(race.getUrl());

        // Combine Date + Time
        if (race.getDate() != null) {
            String dateTimeStr = race.getDate();
            if (race.getTime() != null) {
                dateTimeStr += "T" + race.getTime();
                event.setDateTime(LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER));
            } else {
                event.setDateTime(LocalDate.parse(dateTimeStr).atStartOfDay());
            }
        }

        // Winner - for result api response
        if (includeWinner && race.getResults() != null && !race.getResults().isEmpty()) {
            RacesResponseDto.Result winnerResult = race.getResults()
                    .stream()
                    .filter(r -> "1".equals(r.getPosition()))
                    .findFirst()
                    .orElse(null);
            if (winnerResult != null) {
                String winner = winnerResult.getDriver().getGivenName() + " " +
                        winnerResult.getDriver().getFamilyName();
                event.setWinner(winner);
                event.setStatus(EventStatus.COMPLETED);
            }
        }

        return event;
    }
}
