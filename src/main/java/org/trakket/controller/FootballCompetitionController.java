package org.trakket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trakket.enums.FootballCompetition;
import org.trakket.service.football.FootballCompetitionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/football/competitions")
public class FootballCompetitionController {

    private final FootballCompetitionService footballCompetitionService;

    @GetMapping
    public List<FootballCompetition> getCompetitions() {
        return footballCompetitionService.getAllCompetitions();
    }
}
