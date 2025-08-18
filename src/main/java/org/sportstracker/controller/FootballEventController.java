package org.sportstracker.controller;

import org.sportstracker.dto.WatchStatusUpdateRequest;
import org.sportstracker.dto.WatchStatusUpdateResponse;
import org.sportstracker.enums.WatchedStatus;
import org.sportstracker.model.FootballEvent;
import org.sportstracker.model.FootballEventWatchStatus;
import org.sportstracker.model.User;
import org.sportstracker.service.FootballEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/football/events")
public class FootballEventController {

    private final FootballEventService footballEventService;

    public FootballEventController(FootballEventService footballEventService) {
        this.footballEventService = footballEventService;
    }

    @GetMapping("/{id}")
    public FootballEvent getFootballEvent(@PathVariable Long id) {
        return footballEventService.getEventById(id);
    }

    @GetMapping
    public List<FootballEvent> getAllFootballEvents() {
        return footballEventService.getAllEvents().stream()
                .map(e -> (FootballEvent) e)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public FootballEvent createFootballEvent(@RequestBody FootballEvent event) {
        return (FootballEvent) footballEventService.saveEvent(event);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteFootballEvent(@PathVariable Long id) {
        footballEventService.deleteEvent(id);
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
}
