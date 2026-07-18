package org.trakket.dto.favourites;

import org.trakket.enums.FavouriteType;

public record SetFavouriteRequest(
        FavouriteType type,
        String value,
        boolean favourite
) {}
