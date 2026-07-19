package org.trakket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trakket.dto.favourites.FavouriteEvent;
import org.trakket.dto.favourites.FavouriteEventsResponse;
import org.trakket.dto.favourites.FavouritesResponse;
import org.trakket.dto.football.FootballEventDto;
import org.trakket.dto.motorsport.MotorsportEventDto;
import org.trakket.enums.EventStatus;
import org.trakket.enums.FavouriteType;
import org.trakket.enums.FootballCompetition;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.enums.WatchedStatus;
import org.trakket.mapper.FootballEventMapper;
import org.trakket.mapper.MotorsportEventMapper;
import org.trakket.model.FootballEvent;
import org.trakket.model.FootballEventWatchStatus;
import org.trakket.model.MotorsportEvent;
import org.trakket.model.MotorsportEventWatchStatus;
import org.trakket.model.User;
import org.trakket.model.UserFavourite;
import org.trakket.repository.FootballEventRepository;
import org.trakket.repository.FootballEventWatchStatusRepository;
import org.trakket.repository.MotorsportEventRepository;
import org.trakket.repository.MotorsportEventWatchStatusRepository;
import org.trakket.repository.UserFavouriteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavouritesService {

    private final UserFavouriteRepository userFavouriteRepository;
    private final FootballEventRepository footballEventRepository;
    private final MotorsportEventRepository motorsportEventRepository;
    private final FootballEventWatchStatusRepository footballWatchStatusRepository;
    private final MotorsportEventWatchStatusRepository motorsportWatchStatusRepository;

    // ---- Favourites CRUD ----

    @Transactional(readOnly = true)
    public FavouritesResponse getFavourites(User user) {
        List<UserFavourite> favourites = userFavouriteRepository.findByUser(user);

        List<String> footballCompetitions = new ArrayList<>();
        List<Long> footballTeams = new ArrayList<>();
        List<String> motorsportCompetitions = new ArrayList<>();

        for (UserFavourite fav : favourites) {
            switch (fav.getFavouriteType()) {
                case FOOTBALL_COMPETITION -> footballCompetitions.add(fav.getFavouriteValue());
                case FOOTBALL_TEAM -> footballTeams.add(Long.parseLong(fav.getFavouriteValue()));
                case MOTORSPORT_COMPETITION -> motorsportCompetitions.add(fav.getFavouriteValue());
            }
        }

        return new FavouritesResponse(footballCompetitions, footballTeams, motorsportCompetitions);
    }

    @Transactional
    public FavouritesResponse setFavourite(User user, FavouriteType type, String value, boolean favourite) {
        if (favourite) {
            boolean exists = userFavouriteRepository.existsByUserAndFavouriteTypeAndFavouriteValue(user, type, value);
            if (!exists) {
                UserFavourite userFavourite = new UserFavourite();
                userFavourite.setUser(user);
                userFavourite.setFavouriteType(type);
                userFavourite.setFavouriteValue(value);
                userFavourite.setCreatedAt(LocalDateTime.now());
                userFavouriteRepository.save(userFavourite);
            }
        } else {
            userFavouriteRepository.deleteByUserAndFavouriteTypeAndFavouriteValue(user, type, value);
        }

        return getFavourites(user);
    }

    // ---- Favourite Events Feed ----

    @Transactional(readOnly = true)
    public FavouriteEventsResponse getFavouriteEvents(
            User user,
            int page,
            int pageSize,
            String sports,
            String competitions,
            String teamIds,
            EventStatus eventStatus,
            WatchedStatus watchStatus,
            boolean ascending
    ) {
        // 1. Fetch user's favourites
        List<UserFavourite> favourites = userFavouriteRepository.findByUser(user);

        boolean includeFootball = sports == null || sports.contains("football");
        boolean includeMotorsport = sports == null || sports.contains("motorsport");

        // 2. Parse favourite enums/IDs from the user's saved favourites
        Set<FootballCompetition> favouriteFootballComps = new HashSet<>();
        Set<Long> favouriteTeamIds = new HashSet<>();
        Set<MotorsportCompetition> favouriteMotorsportComps = new HashSet<>();

        for (UserFavourite fav : favourites) {
            switch (fav.getFavouriteType()) {
                case FOOTBALL_COMPETITION -> favouriteFootballComps.add(FootballCompetition.valueOf(fav.getFavouriteValue()));
                case FOOTBALL_TEAM -> favouriteTeamIds.add(Long.parseLong(fav.getFavouriteValue()));
                case MOTORSPORT_COMPETITION -> favouriteMotorsportComps.add(MotorsportCompetition.valueOf(fav.getFavouriteValue()));
            }
        }

        // 3. Apply overrides: if query params provided, use those instead of saved favourites
        if (competitions != null && !competitions.isBlank()) {
            Set<FootballCompetition> footballOverrides = new HashSet<>();
            Set<MotorsportCompetition> motorsportOverrides = new HashSet<>();
            for (String comp : competitions.split(",")) {
                String trimmed = comp.trim();
                try {
                    footballOverrides.add(FootballCompetition.valueOf(trimmed));
                } catch (IllegalArgumentException e) {
                    // not a football competition – try motorsport
                }
                try {
                    motorsportOverrides.add(MotorsportCompetition.valueOf(trimmed));
                } catch (IllegalArgumentException e) {
                    // not a motorsport competition either – skip
                }
            }
            if (!footballOverrides.isEmpty()) favouriteFootballComps = footballOverrides;
            if (!motorsportOverrides.isEmpty()) favouriteMotorsportComps = motorsportOverrides;
        }

        if (teamIds != null && !teamIds.isBlank()) {
            Set<Long> teamIdOverrides = new HashSet<>();
            for (String tid : teamIds.split(",")) {
                teamIdOverrides.add(Long.parseLong(tid.trim()));
            }
            if (!teamIdOverrides.isEmpty()) favouriteTeamIds = teamIdOverrides;
        }

        // 4. Fetch events from each sport
        Sort sort = ascending ? Sort.by("dateTime").ascending() : Sort.by("dateTime").descending();
        // Fetch enough events to cover all of a user's favourites in memory.
        // A very large page size ensures Page.isLast() is true for all realistic
        // favourite sets, so cross-sport sorting + in-memory pagination works.
        int fetchSize = Math.max(pageSize * 3, 5000);

        long totalFootball = 0;
        long totalMotorsport = 0;
        List<FootballEvent> footballEvents = List.of();
        List<MotorsportEvent> motorsportEvents = List.of();
        boolean allFootballFetched = true;
        boolean allMotorsportFetched = true;

        if (includeFootball && (!favouriteFootballComps.isEmpty() || !favouriteTeamIds.isEmpty())) {
            Specification<FootballEvent> footballSpec = buildFootballSpec(favouriteFootballComps, favouriteTeamIds, eventStatus);
            Pageable footballPageable = PageRequest.of(0, fetchSize, sort);
            Page<FootballEvent> footballPage = footballEventRepository.findAll(footballSpec, footballPageable);
            totalFootball = footballPage.getTotalElements();
            footballEvents = footballPage.getContent();
            allFootballFetched = footballPage.isLast();
        }

        if (includeMotorsport && !favouriteMotorsportComps.isEmpty()) {
            Specification<MotorsportEvent> motorsportSpec = buildMotorsportSpec(favouriteMotorsportComps, eventStatus);
            Pageable motorsportPageable = PageRequest.of(0, fetchSize, sort);
            Page<MotorsportEvent> motorsportPage = motorsportEventRepository.findAll(motorsportSpec, motorsportPageable);
            totalMotorsport = motorsportPage.getTotalElements();
            motorsportEvents = motorsportPage.getContent();
            allMotorsportFetched = motorsportPage.isLast();
        }

        long totalElements = totalFootball + totalMotorsport;
        boolean allEventsFetched = allFootballFetched && allMotorsportFetched;
        if (totalElements == 0) {
            return new FavouriteEventsResponse(List.of(), page, pageSize, 0, true);
        }

        // 5. Build unified list with date-time for cross-sport sorting
        List<UnifiedEntry> unified = new ArrayList<>();
        for (FootballEvent fe : footballEvents) {
            unified.add(new UnifiedEntry("football", fe.getId(), fe.getDateTime(), fe));
        }
        for (MotorsportEvent me : motorsportEvents) {
            unified.add(new UnifiedEntry("motorsport", me.getId(), me.getDateTime(), me));
        }

        if (ascending) {
            unified.sort(Comparator.comparing(UnifiedEntry::dateTime));
        } else {
            unified.sort(Comparator.comparing(UnifiedEntry::dateTime).reversed());
        }

        // 6. Batch-fetch watch status for ALL fetched events (before pagination)
        Map<Long, WatchedStatus> footballStatuses = fetchFootballWatchStatuses(user, unified, "football");
        Map<Long, WatchedStatus> motorsportStatuses = fetchMotorsportWatchStatuses(user, unified, "motorsport");

        // 7. Apply watchStatus filter across the full sorted set, then paginate
        if (watchStatus != null) {
            unified = unified.stream()
                    .filter(entry -> {
                        WatchedStatus ws = "football".equals(entry.sport)
                                ? footballStatuses.getOrDefault(entry.eventId, WatchedStatus.UNWATCHED)
                                : motorsportStatuses.getOrDefault(entry.eventId, WatchedStatus.UNWATCHED);
                        return ws == watchStatus;
                    })
                    .collect(Collectors.toList());
        }

        int start = page * pageSize;
        if (start >= unified.size()) {
            // Our in-memory snapshot is exhausted. We're only at the last page if
            // we also fetched every matching row from the DB (not limited by fetchSize).
            return new FavouriteEventsResponse(List.of(), page, pageSize, totalElements, allEventsFetched);
        }
        int end = Math.min(start + pageSize, unified.size());
        List<UnifiedEntry> pageEntries = unified.subList(start, end);

        // 8. Build response DTOs (no filtering needed — already done above)
        List<FavouriteEvent> events = new ArrayList<>(pageEntries.size());
        for (UnifiedEntry entry : pageEntries) {
            String sport = entry.sport;
            WatchedStatus ws;

            if ("football".equals(sport)) {
                FootballEvent fe = (FootballEvent) entry.event;
                ws = footballStatuses.getOrDefault(fe.getId(), WatchedStatus.UNWATCHED);
                FootballEventDto dto = FootballEventMapper.toDto(fe);
                events.add(new FavouriteEvent("football", dto, ws));
            } else {
                MotorsportEvent me = (MotorsportEvent) entry.event;
                ws = motorsportStatuses.getOrDefault(me.getId(), WatchedStatus.UNWATCHED);
                MotorsportEventDto dto = MotorsportEventMapper.toDto(me);
                events.add(new FavouriteEvent("motorsport", dto, ws));
            }
        }

        // We're at the last page only if we exhausted the in-memory list AND
        // every matching row was fetched from the DB (not limited by fetchSize).
        boolean last = end >= unified.size() && allEventsFetched;

        return new FavouriteEventsResponse(events, page, pageSize, totalElements, last);
    }

    // ---- Helpers ----

    private Specification<FootballEvent> buildFootballSpec(
            Set<FootballCompetition> competitions,
            Set<Long> teamIds,
            EventStatus eventStatus
    ) {
        Specification<FootballEvent> spec = Specification.unrestricted();

        if (!competitions.isEmpty() && !teamIds.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            root.get("competition").in(competitions),
                            cb.or(
                                    root.get("homeTeam").get("id").in(teamIds),
                                    root.get("awayTeam").get("id").in(teamIds)
                            )
                    )
            );
        } else if (!competitions.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("competition").in(competitions));
        } else if (!teamIds.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            root.get("homeTeam").get("id").in(teamIds),
                            root.get("awayTeam").get("id").in(teamIds)
                    )
            );
        } else {
            // No filters at all – force empty result
            spec = spec.and((root, query, cb) -> cb.disjunction());
        }

        if (eventStatus != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), eventStatus));
        }

        return spec;
    }

    private Specification<MotorsportEvent> buildMotorsportSpec(
            Set<MotorsportCompetition> competitions,
            EventStatus eventStatus
    ) {
        Specification<MotorsportEvent> spec = Specification.unrestricted();

        if (!competitions.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("competition").in(competitions));
        } else {
            spec = spec.and((root, query, cb) -> cb.disjunction());
        }

        if (eventStatus != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), eventStatus));
        }

        return spec;
    }

    private Map<Long, WatchedStatus> fetchFootballWatchStatuses(User user, List<UnifiedEntry> pageEntries, String sport) {
        List<Long> eventIds = pageEntries.stream()
                .filter(e -> sport.equals(e.sport))
                .map(e -> (Long) e.eventId)
                .toList();

        if (eventIds.isEmpty()) return Map.of();

        return footballWatchStatusRepository.findByUserAndEventIdIn(user, eventIds).stream()
                .collect(Collectors.toMap(
                        ws -> ws.getEvent().getId(),
                        FootballEventWatchStatus::getStatus
                ));
    }

    private Map<Long, WatchedStatus> fetchMotorsportWatchStatuses(User user, List<UnifiedEntry> pageEntries, String sport) {
        List<Long> eventIds = pageEntries.stream()
                .filter(e -> sport.equals(e.sport))
                .map(e -> (Long) e.eventId)
                .toList();

        if (eventIds.isEmpty()) return Map.of();

        return motorsportWatchStatusRepository.findByUserAndEventIdIn(user, eventIds).stream()
                .collect(Collectors.toMap(
                        ws -> ws.getEvent().getId(),
                        MotorsportEventWatchStatus::getStatus
                ));
    }

    private record UnifiedEntry(String sport, long eventId, LocalDateTime dateTime, Object event) {}
}
