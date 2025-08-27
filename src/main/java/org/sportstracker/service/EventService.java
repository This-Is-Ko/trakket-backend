package org.sportstracker.service;

import org.sportstracker.enums.WatchedStatus;
import org.sportstracker.model.Event;
import org.sportstracker.model.EventWatchStatus;
import org.sportstracker.model.User;

import java.util.List;

public interface EventService<T extends Event> {
    T getEventById(Long id);
    List<T> getAllEvents();
    T saveEvent(T event);
    void deleteEvent(Long id);

    EventWatchStatus setWatchStatus(User user, Long eventId, WatchedStatus status);
}
