package org.trakket.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MotorsportCompetition {
    FORMULA_ONE("Formula One"),
    WORLD_ENDURANCE_CHAMPIONSHIP("World Endurance Championship"),
    FORMULA_E("Formula E"),
    MOTOGP("MotoGP");

    private final String displayName;

    MotorsportCompetition(String displayName) {
        this.displayName = displayName;
    }

}
