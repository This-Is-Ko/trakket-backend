package org.sportstracker.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FootballCompetition {
    ENGLISH_PREMIER_LEAGUE("English Premier League"),
    LA_LIGA("La Liga"),
    SERIE_A("Serie A"),
    BUNDESLIGA("Bundesliga");

    private final String displayName;

    FootballCompetition(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
