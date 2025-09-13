package unit.service.football;

import org.junit.jupiter.api.Test;
import org.trakket.enums.FootballCompetition;
import org.trakket.service.football.FootballCompetitionService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FootballCompetitionServiceTest {

    @Test
    void getAllCompetitions_shouldExcludeGivenCompetitions() {
        List<FootballCompetition> excluded = List.of(
                FootballCompetition.FA_CUP,
                FootballCompetition.EFL_CUP
        );
        FootballCompetitionService service = new FootballCompetitionService(excluded);

        List<FootballCompetition> result = service.getAllCompetitions();

        // Expected = all enums except excluded
        List<FootballCompetition> expected = Arrays.stream(FootballCompetition.values())
                .filter(c -> !excluded.contains(c))
                .toList();

        assertThat(result).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(result).doesNotContainAnyElementsOf(excluded);
    }

    @Test
    void getAllCompetitions_shouldReturnAllWhenExcludedIsEmpty() {
        FootballCompetitionService service = new FootballCompetitionService(List.of());

        List<FootballCompetition> result = service.getAllCompetitions();

        assertThat(result).containsExactlyInAnyOrder(FootballCompetition.values());
    }

    @Test
    void getAllCompetitions_shouldReturnNoneWhenAllExcluded() {
        List<FootballCompetition> excluded = Arrays.asList(FootballCompetition.values());
        FootballCompetitionService service = new FootballCompetitionService(excluded);

        List<FootballCompetition> result = service.getAllCompetitions();

        assertThat(result).isEmpty();
    }
}
