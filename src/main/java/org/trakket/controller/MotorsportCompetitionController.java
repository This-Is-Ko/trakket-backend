package org.trakket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.service.motorsport.MotorsportCompetitionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/motorsport/competitions")
public class MotorsportCompetitionController {

    private final MotorsportCompetitionService motorsportCompetitionService;

    @GetMapping
    public List<MotorsportCompetition> getCompetitions() {
        return motorsportCompetitionService.getAllCompetitions();
    }
}
