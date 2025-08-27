package org.sportstracker.controller;

import lombok.RequiredArgsConstructor;
import org.sportstracker.service.motorsport.FormulaOneDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/motorsport/sync")
public class MotorsportEventSyncController implements EventSyncController{

    private final FormulaOneDataService formulaOneDataService;

    @Value("${motorsport.sync.season}")
    private Integer season;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<String> syncEvents() {
        try {
            formulaOneDataService.importLatestRaceResult(season);
            return ResponseEntity.ok("Events synced successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error syncing events: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/init")
    public ResponseEntity<String> initEvents() {
        try {
            formulaOneDataService.importRaceSchedule(season);
            formulaOneDataService.importSeasonResults(season);
            return ResponseEntity.ok("Events initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error syncing events: " + e.getMessage());
        }
    }
}
