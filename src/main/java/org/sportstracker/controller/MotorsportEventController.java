package org.sportstracker.controller;

import lombok.RequiredArgsConstructor;
import org.sportstracker.dto.WatchStatusUpdateRequest;
import org.sportstracker.dto.WatchStatusUpdateResponse;
import org.sportstracker.dto.motorsport.MotorsportEventsWithStatusResponse;
import org.sportstracker.enums.EventStatus;
import org.sportstracker.enums.MotorsportCompetition;
import org.sportstracker.model.FootballEvent;
import org.sportstracker.model.MotorsportEvent;
import org.sportstracker.model.MotorsportEventWatchStatus;
import org.sportstracker.model.User;
import org.sportstracker.service.motorsport.MotorsportEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/motorsport/events")
@RequiredArgsConstructor
public class MotorsportEventController {

    private final MotorsportEventService motorsportEventService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public MotorsportEvent createMotorsportEvent(@RequestBody MotorsportEvent event) {
        return motorsportEventService.saveEvent(event);
    }

    @GetMapping("/with-status")
    public MotorsportEventsWithStatusResponse getEvents(
            Authentication authentication,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "competition", required = false) MotorsportCompetition competition,
            @RequestParam(name = "status", required = false) EventStatus status,
            @RequestParam(name = "ascending", required = false) Boolean ascending
    ) {
        User user = (User) authentication.getDetails();
        return motorsportEventService.getEvents(user, page, pageSize, competition, status, ascending);
    }

    @PostMapping("/status")
    public ResponseEntity<WatchStatusUpdateResponse> setStatus(
            Authentication authentication,
            @RequestBody WatchStatusUpdateRequest request) {

        MotorsportEventWatchStatus watchStatus = motorsportEventService.setWatchStatus((User) authentication.getDetails(), request.getEventId(), request.getStatus());

        WatchStatusUpdateResponse response = new WatchStatusUpdateResponse();
        response.setId(watchStatus.getId());
        response.setStatus(watchStatus.getStatus());
        response.setEventId(watchStatus.getEvent().getId());
        response.setUsername(watchStatus.getUser().getUsername());

        return ResponseEntity.ok(response);
    }
}
