package org.trakket.dto.motorsport;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.trakket.enums.EventStatus;
import org.trakket.enums.MotorsportCompetition;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MotorsportEventDto {
    private Long id;
    private LocalDateTime dateTime;
    private MotorsportCompetition competition;
    private Integer round;
    private String location;
    private EventStatus status;
    private String externalLink;
    private String title;
    private String subtitle;
    private String raceName;
    private String circuitName;
    private String winner;
}
