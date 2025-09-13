package org.trakket.service.football;

import org.trakket.enums.FootballCompetition;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FootballCompetitionService {

    // Exclude list as events are not prepared
    private final List<FootballCompetition> excluded;

    public FootballCompetitionService() {
        this(List.of(
                FootballCompetition.FA_CUP,
                FootballCompetition.EFL_CUP,
                FootballCompetition.UEFA_CHAMPIONS_LEAGUE,
                FootballCompetition.UEFA_WOMENS_CHAMPIONS_LEAGUE
        ));
    }

    public FootballCompetitionService(List<FootballCompetition> excluded) {
        this.excluded = excluded;
    }

    public List<FootballCompetition> getAllCompetitions() {
        return Arrays.stream(FootballCompetition.values())
                .filter(c -> !excluded.contains(c))
                .toList();
    }
}
