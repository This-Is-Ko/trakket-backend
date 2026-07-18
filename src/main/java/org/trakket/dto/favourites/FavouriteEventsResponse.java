package org.trakket.dto.favourites;

import java.util.List;

public record FavouriteEventsResponse(
        List<FavouriteEvent> events,
        int pageNumber,
        int pageSize,
        long totalElements,
        boolean last
) {}
