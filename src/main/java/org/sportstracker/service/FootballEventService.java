package org.sportstracker.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.sportstracker.dto.football.FootballEventDto;
import org.sportstracker.dto.football.FootballEventWithStatus;
import org.sportstracker.dto.football.FootballEventsWithStatusResponse;
import org.sportstracker.enums.EventStatus;
import org.sportstracker.enums.FootballCompetition;
import org.sportstracker.enums.WatchedStatus;
import org.sportstracker.mapper.FootballEventMapper;
import org.sportstracker.model.FootballEvent;
import org.sportstracker.model.FootballEventWatchStatus;
import org.sportstracker.model.User;
import org.sportstracker.repository.FootballEventRepository;
import org.sportstracker.repository.FootballEventWatchStatusRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FootballEventService implements EventService<FootballEvent> {

    private final FootballEventRepository footballEventRepository;
    private final FootballEventWatchStatusRepository watchStatusRepository;

    @Override
    public FootballEvent getEventById(Long id) {
        return footballEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FootballEvent not found with id " + id));
    }

    @Override
    public List<FootballEvent> getAllEvents() {
        return footballEventRepository.findAll();
    }

    private Page<FootballEvent> getEventPage(Integer page, Integer pageSize, FootballCompetition competition, EventStatus status,
                                             Boolean ascending) {
        Sort sort = (ascending != null && ascending)
                ? Sort.by("dateTime").ascending()
                : Sort.by("dateTime").descending();

        Pageable pageable = PageRequest.of(page != null ? page : 0, pageSize != null ? pageSize : 10, sort);

        Specification<FootballEvent> spec = Specification.unrestricted();

        if (competition != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("competition"), competition));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return footballEventRepository.findAll(spec, pageable);
    }

    public FootballEventsWithStatusResponse getEvents(User user, Integer page, Integer pageSize, FootballCompetition competition, EventStatus status, Boolean ascending) {
        if (ascending == null && EventStatus.COMPLETED.equals(status)) {
            ascending = false;
        } else if (ascending == null && EventStatus.SCHEDULED.equals(status)) {
            ascending = true;
        }

        Page<FootballEvent> eventPage = getEventPage(page, pageSize, competition, status, ascending);

        FootballEventsWithStatusResponse response = new FootballEventsWithStatusResponse();

        // Populate pagination details
        response.setPageNumber(eventPage.getNumber());
        response.setPageSize(eventPage.getSize());
        response.setTotalElements(eventPage.getTotalElements());
        response.setLast(eventPage.isLast());

        if (user == null) {
            // No user context so return default
            response.setEvents(eventPage.stream()
                    .map(event -> {
                        FootballEventDto dto = FootballEventMapper.toDto(event);
                        return new FootballEventWithStatus(dto, WatchedStatus.UNWATCHED);
                    })
                    .toList());
        } else {
            // Get all event IDs in the page
            List<Long> eventIds = eventPage.stream()
                    .map(FootballEvent::getId)
                    .toList();

            // Map eventId -> WatchedStatus
            Map<Long, WatchedStatus> statusMap = watchStatusRepository.findByUserAndEventIdIn(user, eventIds).stream()
                    .collect(Collectors.toMap(
                            ws -> ws.getEvent().getId(),
                            FootballEventWatchStatus::getStatus
                    ));

            // Build response using map
            response.setEvents(eventPage.stream()
                    .map(event -> {
                        FootballEventDto dto = FootballEventMapper.toDto(event);
                        WatchedStatus statusEnum = statusMap.getOrDefault(event.getId(), WatchedStatus.UNWATCHED);
                        return new FootballEventWithStatus(dto, statusEnum);
                    })
                    .toList());
        }

        return response;
    }


    @Override
    public FootballEvent saveEvent(FootballEvent event) {
        event.setLastUpdated(LocalDateTime.now());
        return footballEventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        if (!footballEventRepository.existsById(id)) {
            throw new EntityNotFoundException("FootballEvent not found with id " + id);
        }
        footballEventRepository.deleteById(id);
    }

    public FootballEventWatchStatus getWatchStatus(User user, Long eventId) {
        FootballEvent event = footballEventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        return watchStatusRepository.findByEventAndUser(event, user)
                .orElse(null);
    }

    public FootballEventWatchStatus setWatchStatus(User user, Long eventId, WatchedStatus status) {
        FootballEvent event = footballEventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        FootballEventWatchStatus watchStatus = watchStatusRepository.findByEventAndUser(event, user)
                .orElse(new FootballEventWatchStatus());

        watchStatus.setEvent(event);
        watchStatus.setUser(user);
        watchStatus.setStatus(status);
        watchStatus.setUpdatedDateTime(LocalDateTime.now());

        return watchStatusRepository.save(watchStatus);
    }
}
