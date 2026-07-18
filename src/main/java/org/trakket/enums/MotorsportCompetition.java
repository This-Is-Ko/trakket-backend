package org.trakket.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.trakket.config.CompetitionLogoConfig;

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

    public String getName() {
        return this.name();
    }

    public String getLogoUrl() {
        return CompetitionLogoConfig.getLogoUrl(name().toLowerCase());
    }
}
