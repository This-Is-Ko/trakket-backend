package org.sportstracker.converter;

import org.sportstracker.enums.FootballCompetition;
import org.springframework.core.convert.converter.Converter;

public class StringToFootballCompetitionConverter implements Converter<String, FootballCompetition> {
    @Override
    public FootballCompetition convert(String source) {
        for (FootballCompetition fc : FootballCompetition.values()) {
            if (fc.getDisplayName().equalsIgnoreCase(source)) {
                return fc;
            }
        }
        throw new IllegalArgumentException("Invalid FootballCompetition: " + source);
    }
}
