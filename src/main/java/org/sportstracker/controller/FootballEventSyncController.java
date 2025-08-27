package org.sportstracker.controller;

import lombok.RequiredArgsConstructor;
import org.sportstracker.service.football.fantasypremierleague.FantasyPremierLeagueEventSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/football/sync")
public class FootballEventSyncController implements EventSyncController {

    private final FantasyPremierLeagueEventSyncService fantasyPremierLeagueEventSyncService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<String> syncEvents() {
        try {
            fantasyPremierLeagueEventSyncService.syncEvents();
            return ResponseEntity.ok("Events synced successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error syncing events: " + e.getMessage());
        }
    }
}
