package org.sportstracker.dto.football;

import org.sportstracker.enums.WatchedStatus;

public record FootballEventWithStatus (
        FootballEventDto details,
        WatchedStatus status
) {}