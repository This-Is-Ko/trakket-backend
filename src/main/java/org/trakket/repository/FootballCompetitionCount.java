package org.trakket.repository;

import org.trakket.enums.FootballCompetition;

public interface FootballCompetitionCount {
    FootballCompetition getCompetition();
    Long getCnt();
}
