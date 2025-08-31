package org.trakket.repository;

import org.trakket.enums.MotorsportCompetition;

public interface MotorsportCompetitionCount {
    MotorsportCompetition getCompetition();
    Long getCnt();
}
