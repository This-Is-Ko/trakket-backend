package org.trakket.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FootballCompetition {
    ENGLISH_PREMIER_LEAGUE("English Premier League"),
    UEFA_CHAMPIONS_LEAGUE("UEFA Champions league"),
    LA_LIGA("La Liga"),
    SERIE_A("Serie A"),
    BUNDESLIGA("Bundesliga"),
    ENGLISH_WOMENS_SUPER_LEAGUE("English Women's Super League");

    private final String displayName;

    FootballCompetition(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
