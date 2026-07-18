package org.trakket.dto.favourites;

import java.util.List;

public record FavouritesResponse(
        List<String> footballCompetitions,
        List<Long> footballTeams,
        List<String> motorsportCompetitions
) {}
