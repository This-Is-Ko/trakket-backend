package org.trakket.service.motorsport;

import org.springframework.stereotype.Service;
import org.trakket.enums.MotorsportCompetition;

import java.util.Arrays;
import java.util.List;

@Service
public class MotorsportCompetitionService {

    public List<MotorsportCompetition> getAllCompetitions() {
        return Arrays.asList(MotorsportCompetition.values());
    }
}
