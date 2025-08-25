package org.sportstracker.dto;

import org.sportstracker.enums.WatchedStatus;

public record FootballEventWithStatus (
        FootballEventDto details,
        WatchedStatus status
) {}