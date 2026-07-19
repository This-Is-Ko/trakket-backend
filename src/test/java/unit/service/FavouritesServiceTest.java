package unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.trakket.dto.favourites.FavouriteEvent;
import org.trakket.dto.favourites.FavouriteEventsResponse;
import org.trakket.dto.favourites.FavouritesResponse;
import org.trakket.enums.EventStatus;
import org.trakket.enums.FavouriteType;
import org.trakket.enums.FootballCompetition;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.enums.WatchedStatus;
import org.trakket.model.FootballEvent;
import org.trakket.model.FootballEventWatchStatus;
import org.trakket.model.FootballTeam;
import org.trakket.model.MotorsportEvent;
import org.trakket.model.MotorsportEventWatchStatus;
import org.trakket.model.User;
import org.trakket.model.UserFavourite;
import org.trakket.repository.FootballEventRepository;
import org.trakket.repository.FootballEventWatchStatusRepository;
import org.trakket.repository.MotorsportEventRepository;
import org.trakket.repository.MotorsportEventWatchStatusRepository;
import org.trakket.repository.UserFavouriteRepository;
import org.trakket.service.FavouritesService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavouritesServiceTest {

    @Mock
    private UserFavouriteRepository userFavouriteRepository;

    @Mock
    private FootballEventRepository footballEventRepository;

    @Mock
    private MotorsportEventRepository motorsportEventRepository;

    @Mock
    private FootballEventWatchStatusRepository footballWatchStatusRepository;

    @Mock
    private MotorsportEventWatchStatusRepository motorsportWatchStatusRepository;

    @InjectMocks
    private FavouritesService favouritesService;

    private User user;
    private FootballTeam homeTeam;
    private FootballTeam awayTeam;
    private FootballEvent footballEvent1;
    private FootballEvent footballEvent2;
    private FootballEvent footballEvent3;
    private MotorsportEvent motorsportEvent1;
    private MotorsportEvent motorsportEvent2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(99L);
        user.setUsername("testuser");

        homeTeam = new FootballTeam();
        homeTeam.setId(10L);
        homeTeam.setName("Arsenal");
        homeTeam.setLogoUrl("http://logo/arsenal.png");

        awayTeam = new FootballTeam();
        awayTeam.setId(20L);
        awayTeam.setName("Chelsea");
        awayTeam.setLogoUrl("http://logo/chelsea.png");

        footballEvent1 = new FootballEvent();
        footballEvent1.setId(1L);
        footballEvent1.setDateTime(LocalDateTime.of(2025, 6, 1, 15, 0));
        footballEvent1.setCompetition(FootballCompetition.ENGLISH_PREMIER_LEAGUE);
        footballEvent1.setRound(38);
        footballEvent1.setLocation("Emirates");
        footballEvent1.setStatus(EventStatus.COMPLETED);
        footballEvent1.setHomeScore(2);
        footballEvent1.setAwayScore(1);
        footballEvent1.setHomeTeam(homeTeam);
        footballEvent1.setAwayTeam(awayTeam);

        footballEvent2 = new FootballEvent();
        footballEvent2.setId(2L);
        footballEvent2.setDateTime(LocalDateTime.of(2025, 4, 1, 12, 0));
        footballEvent2.setCompetition(FootballCompetition.LA_LIGA);
        footballEvent2.setRound(30);
        footballEvent2.setLocation("Camp Nou");
        footballEvent2.setStatus(EventStatus.COMPLETED);
        footballEvent2.setHomeScore(0);
        footballEvent2.setAwayScore(0);
        footballEvent2.setHomeTeam(awayTeam);
        footballEvent2.setAwayTeam(homeTeam);

        footballEvent3 = new FootballEvent();
        footballEvent3.setId(3L);
        footballEvent3.setDateTime(LocalDateTime.of(2025, 5, 15, 20, 0));
        footballEvent3.setCompetition(FootballCompetition.ENGLISH_PREMIER_LEAGUE);
        footballEvent3.setRound(37);
        footballEvent3.setLocation("Old Trafford");
        footballEvent3.setStatus(EventStatus.SCHEDULED);
        footballEvent3.setHomeTeam(homeTeam);
        footballEvent3.setAwayTeam(awayTeam);

        motorsportEvent1 = new MotorsportEvent();
        motorsportEvent1.setId(10L);
        motorsportEvent1.setDateTime(LocalDateTime.of(2025, 5, 15, 13, 0));
        motorsportEvent1.setCompetition(MotorsportCompetition.FORMULA_ONE);
        motorsportEvent1.setRound(7);
        motorsportEvent1.setLocation("Monaco");
        motorsportEvent1.setStatus(EventStatus.COMPLETED);
        motorsportEvent1.setRaceName("Monaco Grand Prix");
        motorsportEvent1.setCircuitName("Circuit de Monaco");

        motorsportEvent2 = new MotorsportEvent();
        motorsportEvent2.setId(11L);
        motorsportEvent2.setDateTime(LocalDateTime.of(2025, 4, 20, 14, 0));
        motorsportEvent2.setCompetition(MotorsportCompetition.MOTOGP);
        motorsportEvent2.setRound(4);
        motorsportEvent2.setLocation("Jerez");
        motorsportEvent2.setStatus(EventStatus.SCHEDULED);
        motorsportEvent2.setRaceName("Spanish GP");
        motorsportEvent2.setCircuitName("Circuito de Jerez");
    }

    // ======== getFavourites ========

    @Test
    void getFavourites_ShouldReturnEmptyLists_WhenNoFavourites() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of());

        FavouritesResponse response = favouritesService.getFavourites(user);

        assertTrue(response.footballCompetitions().isEmpty());
        assertTrue(response.footballTeams().isEmpty());
        assertTrue(response.motorsportCompetitions().isEmpty());
        verify(userFavouriteRepository, times(1)).findByUser(user);
    }

    @Test
    void getFavourites_ShouldReturnFootballCompetitions() {
        UserFavourite fav = createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE");
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(fav));

        FavouritesResponse response = favouritesService.getFavourites(user);

        assertEquals(List.of("ENGLISH_PREMIER_LEAGUE"), response.footballCompetitions());
        assertTrue(response.footballTeams().isEmpty());
        assertTrue(response.motorsportCompetitions().isEmpty());
    }

    @Test
    void getFavourites_ShouldReturnFootballTeams() {
        UserFavourite fav = createFav(user, FavouriteType.FOOTBALL_TEAM, "42");
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(fav));

        FavouritesResponse response = favouritesService.getFavourites(user);

        assertEquals(List.of(42L), response.footballTeams());
        assertTrue(response.footballCompetitions().isEmpty());
        assertTrue(response.motorsportCompetitions().isEmpty());
    }

    @Test
    void getFavourites_ShouldReturnMotorsportCompetitions() {
        UserFavourite fav = createFav(user, FavouriteType.MOTORSPORT_COMPETITION, "FORMULA_ONE");
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(fav));

        FavouritesResponse response = favouritesService.getFavourites(user);

        assertEquals(List.of("FORMULA_ONE"), response.motorsportCompetitions());
        assertTrue(response.footballCompetitions().isEmpty());
        assertTrue(response.footballTeams().isEmpty());
    }

    @Test
    void getFavourites_ShouldReturnMixedTypes() {
        UserFavourite f1 = createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE");
        UserFavourite f2 = createFav(user, FavouriteType.FOOTBALL_TEAM, "42");
        UserFavourite f3 = createFav(user, FavouriteType.FOOTBALL_TEAM, "87");
        UserFavourite f4 = createFav(user, FavouriteType.MOTORSPORT_COMPETITION, "MOTOGP");
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(f1, f2, f3, f4));

        FavouritesResponse response = favouritesService.getFavourites(user);

        assertEquals(List.of("ENGLISH_PREMIER_LEAGUE"), response.footballCompetitions());
        assertEquals(List.of(42L, 87L), response.footballTeams());
        assertEquals(List.of("MOTOGP"), response.motorsportCompetitions());
    }

    // ======== setFavourite ========

    @Test
    void setFavourite_ShouldAddNew_WhenFavouriteTrue() {
        when(userFavouriteRepository.existsByUserAndFavouriteTypeAndFavouriteValue(
                user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE"))
                .thenReturn(false);
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        FavouritesResponse response = favouritesService.setFavourite(
                user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE", true);

        assertEquals(List.of("ENGLISH_PREMIER_LEAGUE"), response.footballCompetitions());
        verify(userFavouriteRepository, times(1)).save(any(UserFavourite.class));
    }

    @Test
    void setFavourite_ShouldBeIdempotent_WhenAlreadyExists() {
        when(userFavouriteRepository.existsByUserAndFavouriteTypeAndFavouriteValue(
                user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE"))
                .thenReturn(true);
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        FavouritesResponse response = favouritesService.setFavourite(
                user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE", true);

        assertEquals(List.of("ENGLISH_PREMIER_LEAGUE"), response.footballCompetitions());
        verify(userFavouriteRepository, never()).save(any());
    }

    @Test
    void setFavourite_ShouldRemove_WhenFavouriteFalse() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of());

        FavouritesResponse response = favouritesService.setFavourite(
                user, FavouriteType.FOOTBALL_TEAM, "42", false);

        assertTrue(response.footballTeams().isEmpty());
        verify(userFavouriteRepository, times(1))
                .deleteByUserAndFavouriteTypeAndFavouriteValue(user, FavouriteType.FOOTBALL_TEAM, "42");
    }

    @Test
    void setFavourite_ShouldBeIdempotent_WhenRemovingNonExistent() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "LA_LIGA")
        ));

        FavouritesResponse response = favouritesService.setFavourite(
                user, FavouriteType.FOOTBALL_TEAM, "999", false);

        assertEquals(List.of("LA_LIGA"), response.footballCompetitions());
        verify(userFavouriteRepository, times(1))
                .deleteByUserAndFavouriteTypeAndFavouriteValue(user, FavouriteType.FOOTBALL_TEAM, "999");
    }

    @Test
    void setFavourite_ShouldSetCreatedAt_WhenInserting() {
        when(userFavouriteRepository.existsByUserAndFavouriteTypeAndFavouriteValue(
                user, FavouriteType.FOOTBALL_COMPETITION, "LA_LIGA"))
                .thenReturn(false);
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of());

        favouritesService.setFavourite(user, FavouriteType.FOOTBALL_COMPETITION, "LA_LIGA", true);

        verify(userFavouriteRepository, times(1)).save(argThat(uf ->
                uf.getUser() == user
                        && uf.getFavouriteType() == FavouriteType.FOOTBALL_COMPETITION
                        && uf.getFavouriteValue().equals("LA_LIGA")
                        && uf.getCreatedAt() != null
        ));
    }

    // ======== getFavouriteEvents ========

    @Test
    void getFavouriteEvents_ShouldReturnEmpty_WhenNoFavourites() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, null, null, null, false);

        assertTrue(response.events().isEmpty());
        assertEquals(0, response.totalElements());
        assertTrue(response.last());
        verifyNoInteractions(footballEventRepository, motorsportEventRepository);
    }

    @Test
    void getFavouriteEvents_ShouldReturnFootballOnly_WhenOnlyFootballFavs() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent1, footballEvent3));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, null, null, null, false);

        assertEquals(2, response.events().size());
        assertTrue(response.events().stream().allMatch(e -> "football".equals(e.sport())));
        assertEquals(2, response.totalElements());
        verify(motorsportEventRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getFavouriteEvents_ShouldReturnMotorsportOnly_WhenOnlyMotorsportFavs() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.MOTORSPORT_COMPETITION, "FORMULA_ONE")
        ));

        Page<MotorsportEvent> motorsportPage = new PageImpl<>(List.of(motorsportEvent1));
        when(motorsportEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(motorsportPage);
        when(motorsportWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, null, null, null, false);

        assertEquals(1, response.events().size());
        assertEquals("motorsport", response.events().get(0).sport());
        verify(footballEventRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getFavouriteEvents_ShouldMergeBothSports_SortedDescending() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE"),
                createFav(user, FavouriteType.MOTORSPORT_COMPETITION, "FORMULA_ONE")
        ));

        // footballEvent3: 2025-05-15 20:00, motorsportEvent1: 2025-05-15 13:00
        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent3));
        Page<MotorsportEvent> motorsportPage = new PageImpl<>(List.of(motorsportEvent1));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);
        when(motorsportEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(motorsportPage);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());
        when(motorsportWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, null, null, null, false);

        assertEquals(2, response.events().size());
        // Descending: football (May 15 20:00) then motorsport (May 15 13:00)
        assertEquals("football", response.events().get(0).sport());
        assertEquals("motorsport", response.events().get(1).sport());
    }

    @Test
    void getFavouriteEvents_ShouldSortAscending_WhenFlagSet() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE"),
                createFav(user, FavouriteType.MOTORSPORT_COMPETITION, "FORMULA_ONE")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent3));
        Page<MotorsportEvent> motorsportPage = new PageImpl<>(List.of(motorsportEvent1));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);
        when(motorsportEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(motorsportPage);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());
        when(motorsportWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, null, null, null, true);

        assertEquals(2, response.events().size());
        // Ascending: motorsport (May 15 13:00) then football (May 15 20:00)
        assertEquals("motorsport", response.events().get(0).sport());
        assertEquals("football", response.events().get(1).sport());
    }

    @Test
    void getFavouriteEvents_ShouldFilterBySportParam() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE"),
                createFav(user, FavouriteType.MOTORSPORT_COMPETITION, "FORMULA_ONE")
        ));

        Page<MotorsportEvent> motorsportPage = new PageImpl<>(List.of(motorsportEvent1));
        when(motorsportEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(motorsportPage);
        when(motorsportWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, "motorsport", null, null, null, null, false);

        assertEquals(1, response.events().size());
        assertEquals("motorsport", response.events().get(0).sport());
        verify(footballEventRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getFavouriteEvents_ShouldOverrideCompetitions_WhenParamProvided() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        // footballEvent2 is LA_LIGA – should be returned via override
        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent2));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, "LA_LIGA", null, null, null, false);

        assertEquals(1, response.events().size());
        verify(motorsportEventRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getFavouriteEvents_ShouldOverrideTeamIds_WhenParamProvided() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_TEAM, "10")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent1));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        // teamIds param "10" matches saved, but should test override with a different team
        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, "20", null, null, false);

        assertEquals(1, response.events().size());
    }

    @Test
    void getFavouriteEvents_ShouldFilterByEventStatus() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent1));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, null, EventStatus.COMPLETED, null, false);

        assertEquals(1, response.events().size());
    }

    @Test
    void getFavouriteEvents_ShouldFilterByWatchStatus() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent1, footballEvent3));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);

        FootballEventWatchStatus ws = new FootballEventWatchStatus();
        ws.setEvent(footballEvent1);
        ws.setUser(user);
        ws.setStatus(WatchedStatus.LIVE);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of(ws));

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, null, null, WatchedStatus.LIVE, false);

        assertEquals(1, response.events().size());
        assertEquals(WatchedStatus.LIVE, response.events().get(0).status());
    }


    @Test
    void getFavouriteEvents_ShouldPaginateFirstPage() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent1, footballEvent3, footballEvent2));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 2, null, null, null, null, null, true);

        assertEquals(2, response.events().size());
        assertEquals(3, response.totalElements());
        assertFalse(response.last());
    }

    @Test
    void getFavouriteEvents_ShouldPaginateLastPage() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent1, footballEvent3, footballEvent2));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 1, 2, null, null, null, null, null, true);

        assertEquals(1, response.events().size());
        assertEquals(3, response.totalElements());
        assertTrue(response.last());
    }

    @Test
    void getFavouriteEvents_ShouldReturnEmpty_WhenPageBeyondData() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent1));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 10, 12, null, null, null, null, null, false);

        assertTrue(response.events().isEmpty());
        assertTrue(response.last());
    }

    @Test
    void getFavouriteEvents_ShouldMapWatchStatusesCorrectly() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent1));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);

        FootballEventWatchStatus ws = new FootballEventWatchStatus();
        ws.setEvent(footballEvent1);
        ws.setUser(user);
        ws.setStatus(WatchedStatus.REPLAY);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of(ws));

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, null, null, null, false);

        assertEquals(1, response.events().size());
        assertEquals(WatchedStatus.REPLAY, response.events().get(0).status());
    }

    @Test
    void getFavouriteEvents_ShouldDefaultToUnwatched_WhenNoStatusRow() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent1));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, null, null, null, false);

        assertEquals(WatchedStatus.UNWATCHED, response.events().get(0).status());
    }

    @Test
    void getFavouriteEvents_ShouldMatchTeamAsHomeOrAway() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_TEAM, "10")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent1));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, null, null, null, false);

        assertEquals(1, response.events().size());
    }

    @Test
    void getFavouriteEvents_ShouldReturnEmpty_WhenNoMatchingEvents() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE")
        ));

        Page<FootballEvent> emptyPage = new PageImpl<>(List.of());
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, null, null, null, null, false);

        assertTrue(response.events().isEmpty());
        assertEquals(0, response.totalElements());
        assertTrue(response.last());
    }

    @Test
    void getFavouriteEvents_ShouldHandleMixedCompetitionOverride() {
        when(userFavouriteRepository.findByUser(user)).thenReturn(List.of(
                createFav(user, FavouriteType.FOOTBALL_COMPETITION, "ENGLISH_PREMIER_LEAGUE"),
                createFav(user, FavouriteType.MOTORSPORT_COMPETITION, "FORMULA_ONE")
        ));

        Page<FootballEvent> footballPage = new PageImpl<>(List.of(footballEvent2));
        Page<MotorsportEvent> motorsportPage = new PageImpl<>(List.of(motorsportEvent1));
        when(footballEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(footballPage);
        when(motorsportEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(motorsportPage);
        when(footballWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());
        when(motorsportWatchStatusRepository.findByUserAndEventIdIn(eq(user), anyList()))
                .thenReturn(List.of());

        // Override with both sports competitions
        FavouriteEventsResponse response = favouritesService.getFavouriteEvents(
                user, 0, 12, null, "LA_LIGA,FORMULA_ONE", null, null, null, false);

        assertEquals(2, response.events().size());
    }

    // ---- helper ----

    private UserFavourite createFav(User user, FavouriteType type, String value) {
        UserFavourite f = new UserFavourite();
        f.setId(1L);
        f.setUser(user);
        f.setFavouriteType(type);
        f.setFavouriteValue(value);
        f.setCreatedAt(LocalDateTime.now());
        return f;
    }
}
