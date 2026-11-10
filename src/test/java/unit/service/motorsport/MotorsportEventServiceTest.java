package unit.service.motorsport;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trakket.dto.admin.MotorsportEventUpdateRequest;
import org.trakket.enums.EventStatus;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.model.MotorsportEvent;
import org.trakket.model.MotorsportTeam;
import org.trakket.repository.MotorsportEventRepository;
import org.trakket.repository.MotorsportEventWatchStatusRepository;
import org.trakket.service.motorsport.MotorsportEventService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MotorsportEventServiceTest {

    @Mock
    private MotorsportEventRepository motorsportEventRepository;

    @Mock
    private MotorsportEventWatchStatusRepository watchStatusRepository;

    @InjectMocks
    private MotorsportEventService motorsportEventService;

    private MotorsportTeam winnerTeam;
    private MotorsportEvent event;

    @BeforeEach
    void setUp() {
        winnerTeam = new MotorsportTeam();
        winnerTeam.setId(30L);
        winnerTeam.setName("Ferrari");

        event = new MotorsportEvent();
        event.setId(1L);
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setCompetition(MotorsportCompetition.FORMULA_ONE);
        event.setSeason(2024);
        event.setRound(5);
        event.setLocation("Monza");
        event.setStatus(EventStatus.SCHEDULED);
        event.setExternalLink("http://event/1");
        event.setRaceName("Italian Grand Prix");
        event.setCircuitName("Autodromo Nazionale Monza");
        event.setWinner("Charles Leclerc");
        event.setWinnerTeam(winnerTeam);
        event.setFlagUrl("http://flag/italy.png");
    }

    @Test
    void getEventById_ShouldReturnEvent_WhenExists() {
        when(motorsportEventRepository.findById(1L)).thenReturn(Optional.of(event));

        MotorsportEvent result = motorsportEventService.getEventById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(motorsportEventRepository, times(1)).findById(1L);
    }

    @Test
    void getEventById_ShouldThrow_WhenNotFound() {
        when(motorsportEventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> motorsportEventService.getEventById(999L));
        verify(motorsportEventRepository, times(1)).findById(999L);
    }

    @Test
    void updateEvent_ShouldMapAllFieldsAndPersist() {
        when(motorsportEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(motorsportEventRepository.save(any(MotorsportEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MotorsportEventUpdateRequest request = new MotorsportEventUpdateRequest(
                LocalDateTime.now().plusDays(10),
                MotorsportCompetition.MOTOGP,
                EventStatus.COMPLETED,
                8,
                2025,
                "Sepang",
                "http://event/updated",
                "Malaysian Grand Prix",
                "Sepang International Circuit",
                "Francesco Bagnaia",
                "http://flag/malaysia.png"
        );

        MotorsportEvent updated = motorsportEventService.updateEvent(1L, request);

        assertEquals(request.dateTime(), updated.getDateTime());
        assertEquals(MotorsportCompetition.MOTOGP, updated.getCompetition());
        assertEquals(EventStatus.COMPLETED, updated.getStatus());
        assertEquals(8, updated.getRound());
        assertEquals(2025, updated.getSeason());
        assertEquals("Sepang", updated.getLocation());
        assertEquals("http://event/updated", updated.getExternalLink());
        assertEquals("Malaysian Grand Prix", updated.getRaceName());
        assertEquals("Sepang International Circuit", updated.getCircuitName());
        assertEquals("Francesco Bagnaia", updated.getWinner());
        assertEquals("http://flag/malaysia.png", updated.getFlagUrl());
        assertEquals(winnerTeam, updated.getWinnerTeam());
        assertNotNull(updated.getLastUpdated());
        verify(motorsportEventRepository, times(1)).save(event);
    }

    @Test
    void updateEvent_ShouldNormalizeBlankStringsToNull() {
        when(motorsportEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(motorsportEventRepository.save(any(MotorsportEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MotorsportEventUpdateRequest request = new MotorsportEventUpdateRequest(
                LocalDateTime.now().plusDays(10),
                MotorsportCompetition.FORMULA_ONE,
                EventStatus.SCHEDULED,
                8,
                2025,
                "  ",
                "",
                "Malaysian Grand Prix",
                "   ",
                "",
                "  "
        );

        MotorsportEvent updated = motorsportEventService.updateEvent(1L, request);

        assertNull(updated.getLocation());
        assertNull(updated.getExternalLink());
        assertNull(updated.getCircuitName());
        assertNull(updated.getWinner());
        assertNull(updated.getFlagUrl());
    }

    @Test
    void updateEvent_ShouldThrow_WhenNotFound() {
        when(motorsportEventRepository.findById(999L)).thenReturn(Optional.empty());

        MotorsportEventUpdateRequest request = new MotorsportEventUpdateRequest(
                LocalDateTime.now().plusDays(10),
                MotorsportCompetition.FORMULA_ONE,
                EventStatus.SCHEDULED,
                8,
                2025,
                "Sepang",
                "http://event/updated",
                "Malaysian Grand Prix",
                "Sepang International Circuit",
                "Francesco Bagnaia",
                "http://flag/malaysia.png"
        );

        assertThrows(EntityNotFoundException.class, () -> motorsportEventService.updateEvent(999L, request));
        verify(motorsportEventRepository, never()).save(any(MotorsportEvent.class));
    }

    @Test
    void deleteEvent_ShouldDeleteWatchStatusesAndEvent_WhenExists() {
        when(motorsportEventRepository.findById(1L)).thenReturn(Optional.of(event));

        motorsportEventService.deleteEvent(1L);

        verify(watchStatusRepository, times(1)).deleteByEventId(1L);
        verify(motorsportEventRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteEvent_ShouldThrow_WhenNotExists() {
        when(motorsportEventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> motorsportEventService.deleteEvent(999L));
        verify(watchStatusRepository, never()).deleteByEventId(anyLong());
        verify(motorsportEventRepository, never()).deleteById(anyLong());
    }
}
