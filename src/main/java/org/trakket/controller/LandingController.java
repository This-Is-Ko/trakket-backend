package org.trakket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trakket.dto.landing.LandingEventsResponse;
import org.trakket.service.LandingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/landing")
public class LandingController {

    private final LandingService landingService;

    @GetMapping("/events")
    @Cacheable(value = "landingEvents", key = "'landing-page-events'")
    public ResponseEntity<LandingEventsResponse> getLandingEvents() {
        return ResponseEntity.ok(landingService.getLandingEvents());
    }
}
