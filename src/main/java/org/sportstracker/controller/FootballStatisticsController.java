package org.sportstracker.controller;

import lombok.RequiredArgsConstructor;
import org.sportstracker.dto.FootballStatisticsResponse;
import org.sportstracker.model.User;
import org.sportstracker.service.FootballStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/football/statistics")
@RequiredArgsConstructor
public class FootballStatisticsController {

    private final FootballStatisticsService statisticsService;

    @GetMapping
    public ResponseEntity<FootballStatisticsResponse> getStatistics(Authentication authentication) {
        User user = (User) authentication.getDetails();
        return ResponseEntity.ok(statisticsService.getStatistics(user));
    }
}