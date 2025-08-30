package org.trakket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.trakket.enums.FootballCompetition;
import org.trakket.service.football.fantasypremierleague.FantasyPremierLeagueEventSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trakket.service.football.sofascore.SofascoreEventSyncService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/football/sync")
public class FootballEventSyncController implements EventSyncController {

    private final FantasyPremierLeagueEventSyncService fantasyPremierLeagueEventSyncService;
    private final SofascoreEventSyncService sofascoreEventSyncService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<String> syncEvents() {
        try {
//            fantasyPremierLeagueEventSyncService.syncEvents();
            sofascoreEventSyncService.syncEvents();
            return ResponseEntity.ok("Events synced successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error syncing events: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/sofascore")
    public ResponseEntity<String> syncEventsWithSofascore(
            @RequestParam(name = "competition", required = true) FootballCompetition competition,
            @RequestParam(name = "round", required = false) Integer round) {
        try {
            sofascoreEventSyncService.syncRound(competition, round);
            return ResponseEntity.ok("Events synced successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error syncing events: " + e.getMessage());
        }
    }
}
