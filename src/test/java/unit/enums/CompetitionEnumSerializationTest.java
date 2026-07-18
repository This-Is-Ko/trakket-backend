package unit.enums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.trakket.enums.FootballCompetition;
import org.trakket.enums.MotorsportCompetition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompetitionEnumSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void footballCompetition_getName_ShouldReturnEnumConstantName() {
        assertEquals("ENGLISH_PREMIER_LEAGUE", FootballCompetition.ENGLISH_PREMIER_LEAGUE.getName());
        assertEquals("LA_LIGA", FootballCompetition.LA_LIGA.getName());
        assertEquals("UEFA_CHAMPIONS_LEAGUE", FootballCompetition.UEFA_CHAMPIONS_LEAGUE.getName());
    }

    @Test
    void motorsportCompetition_getName_ShouldReturnEnumConstantName() {
        assertEquals("FORMULA_ONE", MotorsportCompetition.FORMULA_ONE.getName());
        assertEquals("MOTOGP", MotorsportCompetition.MOTOGP.getName());
        assertEquals("FORMULA_E", MotorsportCompetition.FORMULA_E.getName());
    }

    @Test
    void footballCompetition_JsonSerialization_ShouldIncludeNameField() throws Exception {
        String json = objectMapper.writeValueAsString(FootballCompetition.ENGLISH_PREMIER_LEAGUE);

        assertTrue(json.contains("\"name\":\"ENGLISH_PREMIER_LEAGUE\""),
                "JSON should contain name field: " + json);
        assertTrue(json.contains("\"displayName\":\"English Premier League\""),
                "JSON should contain displayName field: " + json);
        assertTrue(json.contains("\"gender\":\"M\""),
                "JSON should contain gender field: " + json);
    }

    @Test
    void motorsportCompetition_JsonSerialization_ShouldIncludeNameField() throws Exception {
        String json = objectMapper.writeValueAsString(MotorsportCompetition.FORMULA_ONE);

        assertTrue(json.contains("\"name\":\"FORMULA_ONE\""),
                "JSON should contain name field: " + json);
        assertTrue(json.contains("\"displayName\":\"Formula One\""),
                "JSON should contain displayName field: " + json);
    }
}
