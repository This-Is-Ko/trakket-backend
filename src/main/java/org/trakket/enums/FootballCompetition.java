package org.trakket.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FootballCompetition {
    ENGLISH_PREMIER_LEAGUE("English Premier League", Gender.M),
    FA_CUP("FA Cup", Gender.M),
    EFL_CUP("EFL Cup", Gender.M),
    UEFA_CHAMPIONS_LEAGUE("UEFA Champions league", Gender.M),
    UEFA_EUROPA_LEAGUE("UEFA Europa league", Gender.M),
    LA_LIGA("La Liga", Gender.M),
    SERIE_A("Serie A", Gender.M),
    BUNDESLIGA("Bundesliga", Gender.M),
    ENGLISH_WOMENS_SUPER_LEAGUE("English Women's Super League", Gender.F),
    UEFA_WOMENS_CHAMPIONS_LEAGUE("UEFA Women's Champions league", Gender.F),
    NATIONAL_WOMENS_SOCCER_LEAGUE("National Women's Soccer League", Gender.F);

    private final String displayName;
    private Gender gender;

    FootballCompetition(String displayName, Gender gender) {
        this.displayName = displayName;
        this.gender = gender;
    }
}
