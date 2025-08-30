package org.trakket.dto.football;

import org.trakket.enums.WatchedStatus;

import java.time.LocalDateTime;

public record RecentMatchDto(
        Long id,
        String title,
        String competition,
        LocalDateTime dateTime,
        WatchedStatus watchStatus
) {}