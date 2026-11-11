package unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.trakket.controller.AdminEventController;
import org.trakket.dto.admin.MotorsportEventUpdateRequest;
import org.trakket.enums.EventStatus;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.model.MotorsportEvent;
import org.trakket.service.football.FootballEventService;
import org.trakket.service.motorsport.MotorsportEventService;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Guards the admin update endpoints against losing parameter-name information
 * (the compiler must run with {@code -parameters} so Spring can resolve
 * {@code @PathVariable} arguments).
 */
@ExtendWith(MockitoExtension.class)
class AdminEventControllerTest {

    @Mock
    private FootballEventService footballEventService;

    @Mock
    private MotorsportEventService motorsportEventService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminEventController controller = new AdminEventController(footballEventService, motorsportEventService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void controllerMethodsRetainParameterNames() throws Exception {
        Method method = AdminEventController.class.getMethod(
                "updateMotorsportEvent", Long.class, MotorsportEventUpdateRequest.class);

        String[] names = new StandardReflectionParameterNameDiscoverer().getParameterNames(method);

        assertArrayEquals(new String[]{"id", "request"}, names,
                "Main classes must be compiled with -parameters");
    }

    @Test
    void updateMotorsportEvent_ShouldResolvePathIdAndReturnUpdatedEvent() throws Exception {
        MotorsportEvent saved = new MotorsportEvent();
        saved.setId(883L);
        saved.setCompetition(MotorsportCompetition.FORMULA_ONE);
        saved.setStatus(EventStatus.COMPLETED);
        saved.setRaceName("United States Grand Prix");

        when(motorsportEventService.updateEvent(eq(883L), any())).thenReturn(saved);

        String body = """
                {
                  "dateTime": "2026-10-25T20:00:00",
                  "competition": "FORMULA_ONE",
                  "status": "COMPLETED",
                  "round": 17,
                  "season": null,
                  "location": "Austin, USA",
                  "externalLink": "https://en.wikipedia.org/wiki/2026_United_States_Grand_Prix",
                  "raceName": "United States Grand Prix",
                  "circuitName": "Circuit of the Americas",
                  "winner": "Kimi Antonelli",
                  "flagUrl": "https://flagcdn.com/sg.svg"
                }
                """;

        mockMvc.perform(put("/api/admin/events/motorsport/883")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(883));
    }
}
