package org.sportstracker.service.motorsport;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.sportstracker.dto.motorsport.MotorsportEventDto;
import org.sportstracker.dto.motorsport.MotorsportEventWithStatus;
import org.sportstracker.dto.motorsport.MotorsportEventsWithStatusResponse;
import org.sportstracker.enums.EventStatus;
import org.sportstracker.enums.MotorsportCompetition;
import org.sportstracker.enums.WatchedStatus;
import org.sportstracker.mapper.MotorsportEventMapper;
import org.sportstracker.model.FootballEvent;
import org.sportstracker.model.MotorsportEvent;
import org.sportstracker.model.MotorsportEventWatchStatus;
import org.sportstracker.model.User;
import org.sportstracker.repository.MotorsportEventRepository;
import org.sportstracker.repository.MotorsportEventWatchStatusRepository;
import org.sportstracker.service.EventService;
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
@RequiredArgsConstructor
public class MotorsportEventService implements EventService<MotorsportEvent> {

    private final MotorsportEventRepository motorsportEventRepository;
    private final MotorsportEventWatchStatusRepository watchStatusRepository;

    @Override
    public MotorsportEvent getEventById(Long id) {
        return null;
    }

    @Override
    public List<MotorsportEvent> getAllEvents() {
        return null;
    }

    @Override
    public MotorsportEvent saveEvent(MotorsportEvent event) {
        event.setLastUpdated(LocalDateTime.now());
        return motorsportEventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
    }

    private Page<MotorsportEvent> getEventPage(Integer page, Integer pageSize, MotorsportCompetition competition, EventStatus status, Boolean ascending) {
        Sort sort = (ascending != null && ascending)
                ? Sort.by("dateTime").ascending()
                : Sort.by("dateTime").descending();

        Pageable pageable = PageRequest.of(page != null ? page : 0, pageSize != null ? pageSize : 10, sort);

        Specification<MotorsportEvent> spec = Specification.unrestricted();

        if (competition != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("competition"), competition));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return motorsportEventRepository.findAll(spec, pageable);
    }

    public MotorsportEventsWithStatusResponse getEvents(User user, Integer page, Integer pageSize, MotorsportCompetition competition, EventStatus status, Boolean ascending) {
        if (ascending == null && EventStatus.COMPLETED.equals(status)) {
            ascending = false;
        } else if (ascending == null && EventStatus.SCHEDULED.equals(status)) {
            ascending = true;
        }

        Page<MotorsportEvent> eventPage = getEventPage(page, pageSize, competition, status, ascending);

        MotorsportEventsWithStatusResponse response = new MotorsportEventsWithStatusResponse();
        response.setPageNumber(eventPage.getNumber());
        response.setPageSize(eventPage.getSize());
        response.setTotalElements(eventPage.getTotalElements());
        response.setLast(eventPage.isLast());

        if (user == null) {
            response.setEvents(eventPage.stream()
                    .map(event -> {
                        MotorsportEventDto dto = MotorsportEventMapper.toDto(event);
                        return new MotorsportEventWithStatus(dto, WatchedStatus.UNWATCHED);
                    })
                    .toList());
        } else {
            // Map eventId -> WatchedStatus for the current user
            List<Long> eventIds = eventPage.stream()
                    .map(MotorsportEvent::getId)
                    .toList();

            Map<Long, WatchedStatus> statusMap = watchStatusRepository.findAll().stream() // Replace with custom method if needed
                    .filter(ws -> ws.getUser().equals(user) && eventIds.contains(ws.getEvent().getId()))
                    .collect(Collectors.toMap(
                            ws -> ws.getEvent().getId(),
                            MotorsportEventWatchStatus::getStatus
                    ));

            response.setEvents(eventPage.stream()
                    .map(event -> {
                        MotorsportEventDto dto = MotorsportEventMapper.toDto(event);
                        WatchedStatus statusEnum = statusMap.getOrDefault(event.getId(), WatchedStatus.UNWATCHED);
                        return new MotorsportEventWithStatus(dto, statusEnum);
                    })
                    .toList());
        }

        return response;
    }

    public MotorsportEventWatchStatus setWatchStatus(User user, Long eventId, WatchedStatus status) {
        MotorsportEvent event = motorsportEventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        MotorsportEventWatchStatus watchStatus = watchStatusRepository.findByEventAndUser(event, user)
                .orElse(new MotorsportEventWatchStatus());

        watchStatus.setEvent(event);
        watchStatus.setUser(user);
        watchStatus.setStatus(status);
        watchStatus.setUpdatedDateTime(LocalDateTime.now());

        return watchStatusRepository.save(watchStatus);
    }
}
