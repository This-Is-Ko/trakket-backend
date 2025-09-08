package unit.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trakket.controller.FootballTeamsController;
import org.trakket.dto.football.FootballTeamDto;
import org.trakket.enums.Gender;
import org.trakket.model.FootballTeam;
import org.trakket.service.football.FootballTeamService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class FootballTeamsControllerTest {

    @Mock
    private FootballTeamService footballTeamService;

    @InjectMocks
    private FootballTeamsController controller;

    @Test
    void getAllTeams_ShouldReturnMappedDtos() {
        FootballTeam team1 = new FootballTeam();
        team1.setId(1L);
        team1.setName("Arsenal");
        team1.setShortName("ARS");
        team1.setCountry("England");
        team1.setLogoUrl("http://logo1");
        team1.setAlternativeNames(new String[]{"Gunners"});
        team1.setGender(Gender.M);

        FootballTeam team2 = new FootballTeam();
        team2.setId(2L);
        team2.setName("Barcelona");
        team2.setShortName("BAR");
        team2.setCountry("Spain");
        team2.setLogoUrl("http://logo2");
        team2.setAlternativeNames(new String[]{"Barça"});
        team2.setGender(Gender.M);

        when(footballTeamService.getAllTeams()).thenReturn(List.of(team1, team2));

        List<FootballTeamDto> result = controller.getAllTeams();

        assertEquals(2, result.size());

        FootballTeamDto dto1 = result.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("Arsenal", dto1.getName());
        assertEquals("ARS", dto1.getShortName());
        assertEquals("England", dto1.getCountry());
        assertEquals("http://logo1", dto1.getLogoUrl());
        assertEquals("Gunners", dto1.getAlternativeNames()[0]);
        assertEquals(Gender.M, dto1.getGender());

        FootballTeamDto dto2 = result.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("Barcelona", dto2.getName());
        assertEquals("BAR", dto2.getShortName());
        assertEquals("Spain", dto2.getCountry());
        assertEquals("http://logo2", dto2.getLogoUrl());
        assertEquals("Barça", dto2.getAlternativeNames()[0]);
        assertEquals(Gender.M, dto2.getGender());

        verify(footballTeamService, times(1)).getAllTeams();
    }
}


