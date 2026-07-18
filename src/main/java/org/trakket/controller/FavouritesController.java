package org.trakket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.trakket.dto.favourites.FavouriteEventsResponse;
import org.trakket.dto.favourites.FavouritesResponse;
import org.trakket.dto.favourites.SetFavouriteRequest;
import org.trakket.enums.EventStatus;
import org.trakket.enums.WatchedStatus;
import org.trakket.model.User;
import org.trakket.service.FavouritesService;

@RestController
@RequestMapping("/api/favourites")
@RequiredArgsConstructor
public class FavouritesController {

    private final FavouritesService favouritesService;

    @GetMapping
    public FavouritesResponse getFavourites(Authentication authentication) {
        User user = (User) authentication.getDetails();
        return favouritesService.getFavourites(user);
    }

    @PostMapping("/set")
    public FavouritesResponse setFavourite(
            Authentication authentication,
            @RequestBody SetFavouriteRequest request
    ) {
        User user = (User) authentication.getDetails();
        return favouritesService.setFavourite(user, request.type(), request.value(), request.favourite());
    }

    @GetMapping("/events")
    public FavouriteEventsResponse getFavouriteEvents(
            Authentication authentication,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "pageSize", required = false, defaultValue = "12") int pageSize,
            @RequestParam(name = "sports", required = false) String sports,
            @RequestParam(name = "competitions", required = false) String competitions,
            @RequestParam(name = "teamIds", required = false) String teamIds,
            @RequestParam(name = "eventStatus", required = false) EventStatus eventStatus,
            @RequestParam(name = "watchStatus", required = false) WatchedStatus watchStatus,
            @RequestParam(name = "ascending", required = false, defaultValue = "false") boolean ascending
    ) {
        User user = (User) authentication.getDetails();
        return favouritesService.getFavouriteEvents(
                user, page, pageSize, sports, competitions, teamIds,
                eventStatus, watchStatus, ascending
        );
    }
}
