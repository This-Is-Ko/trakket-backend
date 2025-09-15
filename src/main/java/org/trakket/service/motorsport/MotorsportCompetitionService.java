package org.trakket.service.motorsport;

import org.springframework.stereotype.Service;
import org.trakket.enums.MotorsportCompetition;

import java.util.Arrays;
import java.util.List;

@Service
public class MotorsportCompetitionService {

    private final List<MotorsportCompetition> excluded;

    public MotorsportCompetitionService() {
        this(List.of(
                MotorsportCompetition.FORMULA_E
        ));
    }

    public MotorsportCompetitionService(List<MotorsportCompetition> excluded) {
        this.excluded = excluded;
    }

    public List<MotorsportCompetition> getAllCompetitions() {
        return Arrays.stream(MotorsportCompetition.values())
                .filter(c -> !excluded.contains(c))
                .toList();
    }
}