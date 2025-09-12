package org.trakket.converter;

import org.trakket.enums.MotorsportCompetition;
import org.springframework.core.convert.converter.Converter;

public class StringToMotorsportCompetitionConverter implements Converter<String, MotorsportCompetition> {
    @Override
    public MotorsportCompetition convert(String source) {
        for (MotorsportCompetition mc : MotorsportCompetition.values()) {
            if (mc.name().equalsIgnoreCase(source) || mc.getDisplayName().equalsIgnoreCase(source)) {
                return mc;
            }
        }
        throw new IllegalArgumentException("Invalid MotorsportCompetition: " + source);
    }
}
