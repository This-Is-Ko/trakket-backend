package org.sportstracker.controller;

import lombok.RequiredArgsConstructor;
import org.sportstracker.dto.football.FootballEventsWithStatusResponse;
import org.sportstracker.dto.WatchStatusUpdateRequest;
import org.sportstracker.dto.WatchStatusUpdateResponse;
import org.sportstracker.enums.EventStatus;
import org.sportstracker.enums.FootballCompetition;
import org.sportstracker.model.FootballEvent;
import org.sportstracker.model.FootballEventWatchStatus;
import org.sportstracker.model.User;
import org.sportstracker.service.FootballEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/football/events")
public class FootballEventController {

    private final FootballEventService footballEventService;

    @GetMapping
    public FootballEventsWithStatusResponse getFootballEvents(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "competition", required = false) FootballCompetition competition,
            @RequestParam(name = "status", required = false) EventStatus status,
            @RequestParam(name = "ascending ", required = false) Boolean ascending) {
        return footballEventService.getEvents(null, page, pageSize, competition, status, ascending);
    }

    @GetMapping("/{id}")
    public FootballEvent getFootballEvent(@PathVariable Long id) {
        return footballEventService.getEventById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public FootballEvent createFootballEvent(@RequestBody FootballEvent event) {
        return footballEventService.saveEvent(event);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteFootballEvent(@PathVariable Long id) {
        footballEventService.deleteEvent(id);
    }

    @GetMapping("/with-status")
    public FootballEventsWithStatusResponse getFootballEventsWithStatus(
            Authentication authentication,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "competition", required = false) FootballCompetition competition,
            @RequestParam(name = "status", required = false) EventStatus status,
            @RequestParam(name = "ascending ", required = false) Boolean ascending) {

        User user = (User) authentication.getDetails();
        return footballEventService.getEvents(user, page, pageSize, competition, status, ascending);
    }

    @GetMapping("/status")
    public ResponseEntity<WatchStatusUpdateResponse> getStatus(
            Authentication authentication,
            @RequestParam Long eventId) {

        FootballEventWatchStatus watchStatus = footballEventService.getWatchStatus((User) authentication.getDetails(), eventId);
        if (watchStatus == null) {
            return ResponseEntity.notFound().build();
        }

        WatchStatusUpdateResponse response = new WatchStatusUpdateResponse();
        response.setId(watchStatus.getId());
        response.setStatus(watchStatus.getStatus());
        response.setEventId(watchStatus.getEvent().getId());
        response.setUsername(watchStatus.getUser().getUsername());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/status")
    public ResponseEntity<WatchStatusUpdateResponse> setStatus(
            Authentication authentication,
            @RequestBody WatchStatusUpdateRequest request) {

        FootballEventWatchStatus watchStatus = footballEventService.setWatchStatus((User) authentication.getDetails(), request.getEventId(), request.getStatus());

        WatchStatusUpdateResponse response = new WatchStatusUpdateResponse();
        response.setId(watchStatus.getId());
        response.setStatus(watchStatus.getStatus());
        response.setEventId(watchStatus.getEvent().getId());
        response.setUsername(watchStatus.getUser().getUsername());

        return ResponseEntity.ok(response);
    }
}
