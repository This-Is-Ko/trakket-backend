package unit.service.football;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.trakket.dto.football.FootballEventsWithStatusResponse;
import org.trakket.enums.EventStatus;
import org.trakket.enums.FootballCompetition;
import org.trakket.enums.WatchedStatus;
import org.trakket.model.FootballEvent;
import org.trakket.model.FootballEventWatchStatus;
import org.trakket.model.FootballTeam;
import org.trakket.model.User;
import org.trakket.repository.FootballEventRepository;
import org.trakket.repository.FootballEventWatchStatusRepository;
import org.trakket.service.football.FootballEventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FootballEventServiceTest {

    @Mock
    private FootballEventRepository footballEventRepository;

    @Mock
    private FootballEventWatchStatusRepository watchStatusRepository;

    @InjectMocks
    private FootballEventService footballEventService;

    private FootballTeam homeTeam;
    private FootballTeam awayTeam;
    private FootballEvent event1;
    private FootballEvent event2;
    private User user;

    @BeforeEach
    void setUp() {
        homeTeam = new FootballTeam();
        homeTeam.setId(10L);
        homeTeam.setName("Arsenal");
        homeTeam.setLogoUrl("http://logo/arsenal.png");

        awayTeam = new FootballTeam();
        awayTeam.setId(20L);
        awayTeam.setName("Manchester United");
        awayTeam.setLogoUrl("http://logo/chelsea.png");

        event1 = new FootballEvent();
        event1.setId(1L);
        event1.setDateTime(LocalDateTime.now().plusDays(1));
        event1.setCompetition(FootballCompetition.ENGLISH_PREMIER_LEAGUE);
        event1.setRound(12);
        event1.setLocation("Emirates Stadium");
        event1.setStatus(EventStatus.SCHEDULED);
        event1.setExternalLink("http://event/1");
        event1.setHomeTeam(homeTeam);
        event1.setAwayTeam(awayTeam);

        event2 = new FootballEvent();
        event2.setId(2L);
        event2.setDateTime(LocalDateTime.now().minusDays(1));
        event2.setCompetition(FootballCompetition.ENGLISH_PREMIER_LEAGUE);
        event2.setRound(11);
        event2.setLocation("Stamford Bridge");
        event2.setStatus(EventStatus.COMPLETED);
        event2.setExternalLink("http://event/2");
        event2.setHomeTeam(awayTeam);
        event2.setAwayTeam(homeTeam);

        user = new User();
        user.setId(99L);
        user.setUsername("tester");
    }

    @Test
    void getEventById_ShouldReturnEvent_WhenExists() {
        when(footballEventRepository.findById(1L)).thenReturn(Optional.of(event1));

        FootballEvent result = footballEventService.getEventById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(footballEventRepository, times(1)).findById(1L);
    }

    @Test
    void getEventById_ShouldThrow_WhenNotFound() {
        when(footballEventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> footballEventService.getEventById(999L));
        verify(footballEventRepository, times(1)).findById(999L);
    }

    @Test
    void getAllEvents_ShouldReturnList() {
        when(footballEventRepository.findAll()).thenReturn(List.of(event1, event2));

        List<FootballEvent> events = footballEventService.getAllEvents();

        assertEquals(2, events.size());
        verify(footballEventRepository, times(1)).findAll();
    }

    @Test
    void getEvents_ShouldReturnUnwatched_WhenUserNull() {
        Page<FootballEvent> page = new PageImpl<>(List.of(event1, event2));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        FootballEventsWithStatusResponse response = footballEventService.getEvents(
                null,
                0,
                10,
                FootballCompetition.ENGLISH_PREMIER_LEAGUE,
                EventStatus.SCHEDULED,
                true,
                null,
                null
        );

        assertNotNull(response);
        assertEquals(2, response.getEvents().size());
        assertTrue(response.getEvents().stream().allMatch(e -> e.status() == WatchedStatus.UNWATCHED));
        verify(watchStatusRepository, never()).findByUserAndEventIdIn(any(), any());
    }

    @Test
    void getEvents_ShouldReturnMappedStatuses_WhenUserProvided() {
        Page<FootballEvent> page = new PageImpl<>(List.of(event1, event2));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        FootballEventWatchStatus ws1 = new FootballEventWatchStatus();
        ws1.setEvent(event1);
        ws1.setUser(user);
        ws1.setStatus(WatchedStatus.REPLAY);

        when(watchStatusRepository.findByUserAndEventIdIn(eq(user), any()))
                .thenReturn(List.of(ws1));

        FootballEventsWithStatusResponse response = footballEventService.getEvents(
                user,
                0,
                10,
                FootballCompetition.ENGLISH_PREMIER_LEAGUE,
                EventStatus.SCHEDULED,
                true,
                null,
                null
        );

        assertNotNull(response);
        assertEquals(2, response.getEvents().size());
        assertEquals(WatchedStatus.REPLAY, response.getEvents().get(0).status());
        assertEquals(WatchedStatus.UNWATCHED, response.getEvents().get(1).status());
        verify(watchStatusRepository, times(1)).findByUserAndEventIdIn(eq(user), any());
    }

    @Test
    void getEvents_ShouldDefaultAscendingTrue_ForScheduled_WhenAscendingNull() {
        Page<FootballEvent> page = new PageImpl<>(List.of(event1));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        footballEventService.getEvents(
                null,
                0,
                10,
                FootballCompetition.ENGLISH_PREMIER_LEAGUE,
                EventStatus.SCHEDULED,
                null,
                null,
                null
        );

        verify(footballEventRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        Sort sort = used.getSort();
        assertTrue(sort.getOrderFor("dateTime").isAscending());
    }

    @Test
    void getEvents_ShouldDefaultAscendingFalse_ForCompleted_WhenAscendingNull() {
        Page<FootballEvent> page = new PageImpl<>(List.of(event2));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        footballEventService.getEvents(
                null,
                0,
                10,
                FootballCompetition.ENGLISH_PREMIER_LEAGUE,
                EventStatus.COMPLETED,
                null,
                null,
                null
        );

        verify(footballEventRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        Sort sort = used.getSort();
        assertTrue(sort.getOrderFor("dateTime").isDescending());
    }

    @Test
    void saveEvent_ShouldSetLastUpdatedAndPersist() {
        when(footballEventRepository.save(any(FootballEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FootballEvent toSave = new FootballEvent();
        toSave.setHomeTeam(homeTeam);
        toSave.setAwayTeam(awayTeam);
        toSave.setCompetition(FootballCompetition.ENGLISH_PREMIER_LEAGUE);
        toSave.setStatus(EventStatus.SCHEDULED);
        toSave.setDateTime(LocalDateTime.now().plusDays(3));

        FootballEvent saved = footballEventService.saveEvent(toSave);

        assertNotNull(saved.getLastUpdated());
        verify(footballEventRepository, times(1)).save(any(FootballEvent.class));
    }

    @Test
    void deleteEvent_ShouldDelete_WhenExists() {
        when(footballEventRepository.existsById(1L)).thenReturn(true);

        footballEventService.deleteEvent(1L);

        verify(footballEventRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteEvent_ShouldThrow_WhenNotExists() {
        when(footballEventRepository.existsById(123L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> footballEventService.deleteEvent(123L));
        verify(footballEventRepository, never()).deleteById(anyLong());
    }

    @Test
    void getWatchStatus_ShouldReturnNull_WhenWatchStatusNotFound() {
        when(footballEventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(watchStatusRepository.findByEventAndUser(event1, user)).thenReturn(Optional.empty());

        FootballEventWatchStatus result = footballEventService.getWatchStatus(user, 1L);

        assertNull(result);
        verify(watchStatusRepository, times(1)).findByEventAndUser(event1, user);
    }

    @Test
    void getWatchStatus_ShouldThrow_WhenEventNotFound() {
        when(footballEventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> footballEventService.getWatchStatus(user, 1L));
        verify(watchStatusRepository, never()).findByEventAndUser(any(), any());
    }

    @Test
    void setWatchStatus_ShouldCreateNew_WhenNotExisting() {
        when(footballEventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(watchStatusRepository.findByEventAndUser(event1, user)).thenReturn(Optional.empty());
        when(watchStatusRepository.save(any(FootballEventWatchStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FootballEventWatchStatus saved = footballEventService.setWatchStatus(user, 1L, WatchedStatus.HIGHLIGHTS);

        assertNotNull(saved);
        assertEquals(event1, saved.getEvent());
        assertEquals(user, saved.getUser());
        assertEquals(WatchedStatus.HIGHLIGHTS, saved.getStatus());
        assertNotNull(saved.getUpdatedDateTime());
        verify(watchStatusRepository, times(1)).save(any(FootballEventWatchStatus.class));
    }

    @Test
    void setWatchStatus_ShouldUpdate_WhenExisting() {
        when(footballEventRepository.findById(2L)).thenReturn(Optional.of(event2));

        FootballEventWatchStatus existing = new FootballEventWatchStatus();
        existing.setEvent(event2);
        existing.setUser(user);
        existing.setStatus(WatchedStatus.LIVE);

        when(watchStatusRepository.findByEventAndUser(event2, user)).thenReturn(Optional.of(existing));
        when(watchStatusRepository.save(any(FootballEventWatchStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FootballEventWatchStatus saved = footballEventService.setWatchStatus(user, 2L, WatchedStatus.IN_PERSON);

        assertNotNull(saved);
        assertEquals(WatchedStatus.IN_PERSON, saved.getStatus());
        assertEquals(event2, saved.getEvent());
        assertEquals(user, saved.getUser());
        assertNotNull(saved.getUpdatedDateTime());
        verify(watchStatusRepository, times(1)).save(existing);
    }

    @Test
    void setWatchStatus_ShouldThrow_WhenEventNotFound() {
        when(footballEventRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> footballEventService.setWatchStatus(user, 404L, WatchedStatus.REPLAY));
        verify(watchStatusRepository, never()).save(any());
    }
}


