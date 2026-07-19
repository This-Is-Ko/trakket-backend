package org.trakket.dto.favourites;

import org.trakket.enums.WatchedStatus;

public record FavouriteEvent(
        String sport,
        Object details,
        WatchedStatus status
) {}
