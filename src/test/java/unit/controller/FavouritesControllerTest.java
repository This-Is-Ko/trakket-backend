package unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.trakket.controller.FavouritesController;
import org.trakket.dto.favourites.FavouriteEventsResponse;
import org.trakket.dto.favourites.FavouritesResponse;
import org.trakket.dto.favourites.SetFavouriteRequest;
import org.trakket.enums.EventStatus;
import org.trakket.enums.FavouriteType;
import org.trakket.enums.WatchedStatus;
import org.trakket.model.User;
import org.trakket.service.FavouritesService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavouritesControllerTest {

    @Mock
    private FavouritesService favouritesService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FavouritesController controller;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(authentication.getDetails()).thenReturn(user);
    }

    @Test
    void getFavourites_ShouldDelegateToService() {
        FavouritesResponse expected = new FavouritesResponse(
                List.of("ENGLISH_PREMIER_LEAGUE"),
                List.of(42L),
                List.of()
        );
        when(favouritesService.getFavourites(user)).thenReturn(expected);

        FavouritesResponse response = controller.getFavourites(authentication);

        assertEquals(expected, response);
        verify(favouritesService, times(1)).getFavourites(user);
    }

    @Test
    void setFavourite_ShouldDelegateToService() {
        SetFavouriteRequest request = new SetFavouriteRequest(
                FavouriteType.FOOTBALL_COMPETITION,
                "LA_LIGA",
                true
        );

        FavouritesResponse expected = new FavouritesResponse(
                List.of("ENGLISH_PREMIER_LEAGUE", "LA_LIGA"),
                List.of(),
                List.of()
        );
        when(favouritesService.setFavourite(user, FavouriteType.FOOTBALL_COMPETITION, "LA_LIGA", true))
                .thenReturn(expected);

        FavouritesResponse response = controller.setFavourite(authentication, request);

        assertEquals(expected, response);
        verify(favouritesService, times(1))
                .setFavourite(user, FavouriteType.FOOTBALL_COMPETITION, "LA_LIGA", true);
    }

    @Test
    void setFavourite_ShouldPassRemoveRequest() {
        SetFavouriteRequest request = new SetFavouriteRequest(
                FavouriteType.FOOTBALL_TEAM,
                "42",
                false
        );

        FavouritesResponse expected = new FavouritesResponse(List.of(), List.of(), List.of());
        when(favouritesService.setFavourite(user, FavouriteType.FOOTBALL_TEAM, "42", false))
                .thenReturn(expected);

        FavouritesResponse response = controller.setFavourite(authentication, request);

        assertEquals(expected, response);
        verify(favouritesService, times(1))
                .setFavourite(user, FavouriteType.FOOTBALL_TEAM, "42", false);
    }

    @Test
    void getFavouriteEvents_ShouldUseDefaults_WhenNoParams() {
        FavouriteEventsResponse expected = new FavouriteEventsResponse(
                List.of(), 0, 12, 0, true
        );
        when(favouritesService.getFavouriteEvents(
                eq(user), eq(0), eq(12), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(false)
        )).thenReturn(expected);

        FavouriteEventsResponse response = controller.getFavouriteEvents(
                authentication, null, null, null, null, null, null, null, null);

        assertEquals(expected, response);
        verify(favouritesService, times(1)).getFavouriteEvents(
                user, 0, 12, null, null, null, null, null, false
        );
    }

    @Test
    void getFavouriteEvents_ShouldPassAllParams() {
        FavouriteEventsResponse expected = new FavouriteEventsResponse(
                List.of(), 0, 20, 0, true
        );
        when(favouritesService.getFavouriteEvents(
                eq(user), eq(0), eq(20), eq("football"), eq("ENGLISH_PREMIER_LEAGUE"),
                eq("42"), eq(EventStatus.SCHEDULED), eq(WatchedStatus.UNWATCHED), eq(true)
        )).thenReturn(expected);

        FavouriteEventsResponse response = controller.getFavouriteEvents(
                authentication, 0, 20, "football", "ENGLISH_PREMIER_LEAGUE",
                "42", EventStatus.SCHEDULED, WatchedStatus.UNWATCHED, true);

        assertEquals(expected, response);
        verify(favouritesService, times(1)).getFavouriteEvents(
                user, 0, 20, "football", "ENGLISH_PREMIER_LEAGUE",
                "42", EventStatus.SCHEDULED, WatchedStatus.UNWATCHED, true
        );
    }

    @Test
    void getFavouriteEvents_ShouldPassSportsFilter() {
        FavouriteEventsResponse expected = new FavouriteEventsResponse(List.of(), 0, 12, 0, true);
        when(favouritesService.getFavouriteEvents(
                eq(user), eq(0), eq(12), eq("motorsport"), isNull(), isNull(),
                isNull(), isNull(), eq(false)
        )).thenReturn(expected);

        FavouriteEventsResponse response = controller.getFavouriteEvents(
                authentication, 0, 12, "motorsport", null, null, null, null, false);

        assertEquals(expected, response);
        verify(favouritesService, times(1)).getFavouriteEvents(
                user, 0, 12, "motorsport", null, null, null, null, false
        );
    }
}
