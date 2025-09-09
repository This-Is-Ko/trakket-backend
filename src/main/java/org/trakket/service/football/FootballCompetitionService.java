package org.trakket.service.football;

import org.trakket.enums.FootballCompetition;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FootballCompetitionService {

    public List<FootballCompetition> getAllCompetitions() {
        return Arrays.asList(FootballCompetition.values());
    }
}
