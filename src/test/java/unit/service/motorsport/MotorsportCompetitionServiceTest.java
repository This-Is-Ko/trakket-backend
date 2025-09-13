package unit.service.motorsport;

import org.junit.jupiter.api.Test;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.service.motorsport.MotorsportCompetitionService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MotorsportCompetitionServiceTest {

    @Test
    void getAllCompetitions_shouldExcludeGivenCompetitions() {
        List<MotorsportCompetition> excluded = List.of(MotorsportCompetition.FORMULA_E);
        MotorsportCompetitionService service = new MotorsportCompetitionService(excluded);

        List<MotorsportCompetition> result = service.getAllCompetitions();

        List<MotorsportCompetition> expected = Arrays.stream(MotorsportCompetition.values())
                .filter(c -> !excluded.contains(c))
                .toList();

        assertThat(result).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(result).doesNotContainAnyElementsOf(excluded);
    }

    @Test
    void getAllCompetitions_shouldReturnAllWhenExcludedIsEmpty() {
        MotorsportCompetitionService service = new MotorsportCompetitionService(List.of());

        List<MotorsportCompetition> result = service.getAllCompetitions();

        assertThat(result).containsExactlyInAnyOrder(MotorsportCompetition.values());
    }

    @Test
    void getAllCompetitions_shouldReturnNoneWhenAllExcluded() {
        List<MotorsportCompetition> excluded = Arrays.asList(MotorsportCompetition.values());
        MotorsportCompetitionService service = new MotorsportCompetitionService(excluded);

        List<MotorsportCompetition> result = service.getAllCompetitions();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllCompetitions_defaultConstructorShouldExcludeDefaults() {
        MotorsportCompetitionService service = new MotorsportCompetitionService();

        List<MotorsportCompetition> result = service.getAllCompetitions();

        // build expected dynamically from default excluded
        List<MotorsportCompetition> expected = Arrays.stream(MotorsportCompetition.values())
                .filter(c -> c != MotorsportCompetition.FORMULA_E && c != MotorsportCompetition.MOTOGP)
                .toList();

        assertThat(result).containsExactlyInAnyOrderElementsOf(expected);
    }
}
