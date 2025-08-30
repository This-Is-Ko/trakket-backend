package org.trakket.dto.football;

import org.trakket.enums.WatchedStatus;

public record FootballEventWithStatus (
        FootballEventDto details,
        WatchedStatus status
) {}