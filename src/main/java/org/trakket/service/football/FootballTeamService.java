package org.trakket.service.football;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.trakket.model.FootballTeam;
import org.trakket.repository.FootballTeamRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FootballTeamService {

    private final FootballTeamRepository footballTeamRepository;

    public List<FootballTeam> getAllTeams() {
        return footballTeamRepository.findAll();
    }
}


