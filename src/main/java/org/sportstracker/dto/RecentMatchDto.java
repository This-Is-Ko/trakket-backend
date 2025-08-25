package org.sportstracker.dto;

import org.sportstracker.enums.WatchedStatus;

import java.time.LocalDateTime;

public record RecentMatchDto(
        Long id,
        String title,
        String competition,
        LocalDateTime dateTime,
        WatchedStatus watchStatus
) {}