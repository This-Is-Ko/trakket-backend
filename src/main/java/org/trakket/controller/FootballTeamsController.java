package org.trakket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trakket.dto.football.FootballTeamDto;
import org.trakket.mapper.FootballTeamMapper;
import org.trakket.service.football.FootballTeamService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/football/teams")
public class FootballTeamsController {

    private final FootballTeamService footballTeamService;

    @GetMapping
    public List<FootballTeamDto> getAllTeams() {
        return footballTeamService.getAllTeams().stream()
                .map(FootballTeamMapper::toDto)
                .toList();
    }
}


