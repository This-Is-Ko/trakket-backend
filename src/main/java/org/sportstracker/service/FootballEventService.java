package org.sportstracker.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.sportstracker.enums.WatchedStatus;
import org.sportstracker.model.FootballEvent;
import org.sportstracker.model.FootballEventWatchStatus;
import org.sportstracker.model.User;
import org.sportstracker.repository.FootballEventRepository;
import org.sportstracker.repository.FootballEventWatchStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FootballEventService implements EventService<FootballEvent> {

    private final FootballEventRepository eventRepository;
    private final FootballEventWatchStatusRepository watchStatusRepository;

    public FootballEventService(FootballEventRepository eventRepository,
                                FootballEventWatchStatusRepository watchStatusRepository) {
        this.eventRepository = eventRepository;
        this.watchStatusRepository = watchStatusRepository;
    }

    @Override
    public FootballEvent getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FootballEvent not found with id " + id));
    }

    @Override
    public List<FootballEvent> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public FootballEvent saveEvent(FootballEvent event) {
        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("FootballEvent not found with id " + id);
        }
        eventRepository.deleteById(id);
    }

    public FootballEventWatchStatus setWatchStatus(User user, Long eventId, WatchedStatus status) {
        FootballEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        FootballEventWatchStatus watchStatus = watchStatusRepository.findByEventAndUser(event, user)
                .orElse(new FootballEventWatchStatus());

        watchStatus.setEvent(event);
        watchStatus.setUser(user);
        watchStatus.setStatus(status);

        return watchStatusRepository.save(watchStatus);
    }

    public FootballEventWatchStatus getWatchStatus(User user, Long eventId) {
        FootballEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        return watchStatusRepository.findByEventAndUser(event, user)
                .orElse(null);
    }
}
