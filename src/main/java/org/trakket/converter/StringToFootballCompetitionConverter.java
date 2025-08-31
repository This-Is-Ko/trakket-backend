package org.trakket.converter;

import org.springframework.stereotype.Component;
import org.trakket.enums.FootballCompetition;
import org.springframework.core.convert.converter.Converter;

@Component
public class StringToFootballCompetitionConverter implements Converter<String, FootballCompetition> {
    @Override
    public FootballCompetition convert(String source) {
        for (FootballCompetition fc : FootballCompetition.values()) {
            if (fc.name().equalsIgnoreCase(source) || fc.getDisplayName().equalsIgnoreCase(source)) {
                return fc;
            }
        }
        throw new IllegalArgumentException("Invalid FootballCompetition: " + source);
    }
}
