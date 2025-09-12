package org.trakket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.service.motorsport.MotorsportSyncService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/motorsport/sync")
public class MotorsportEventSyncController implements EventSyncController{

    private final MotorsportSyncService motorsportSyncService;


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<String> syncEvents() {
        try {
            motorsportSyncService.syncEvents();
            return ResponseEntity.ok("Events synced successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error syncing events: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/init")
    public ResponseEntity<String> initEvents(
            @RequestParam(name = "competition", required = false) MotorsportCompetition competition
    ) {
        try {
            motorsportSyncService.initCompetitionEvents(competition);
            return ResponseEntity.ok("Events initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error syncing events: " + e.getMessage());
        }
    }
}
