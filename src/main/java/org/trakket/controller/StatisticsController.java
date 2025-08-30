package org.trakket.controller;

import lombok.RequiredArgsConstructor;
import org.trakket.dto.statistics.StatisticsResponse;
import org.trakket.model.User;
import org.trakket.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public ResponseEntity<StatisticsResponse> getStatistics(Authentication authentication) {
        User user = (User) authentication.getDetails();
        return ResponseEntity.ok(statisticsService.getStatistics(user));
    }
}