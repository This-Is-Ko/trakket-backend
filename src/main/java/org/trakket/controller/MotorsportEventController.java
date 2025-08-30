package org.trakket.controller;

import lombok.RequiredArgsConstructor;
import org.trakket.dto.WatchStatusUpdateRequest;
import org.trakket.dto.WatchStatusUpdateResponse;
import org.trakket.dto.motorsport.MotorsportEventsWithStatusResponse;
import org.trakket.enums.EventStatus;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.model.MotorsportEvent;
import org.trakket.model.MotorsportEventWatchStatus;
import org.trakket.model.User;
import org.trakket.service.motorsport.MotorsportEventService;
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
