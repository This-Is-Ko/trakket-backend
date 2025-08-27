package org.sportstracker.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MotorsportCompetition {
    FORMULA_ONE("Formula One"),
    WORLD_ENDURANCE_CHAMPIONSHIP("World Endurance Championship"),
    FORMULA_E("Formula E");

    private final String displayName;

    MotorsportCompetition(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
