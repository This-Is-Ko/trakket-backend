package org.trakket.service;

import org.trakket.enums.WatchedStatus;
import org.trakket.model.Event;
import org.trakket.model.EventWatchStatus;
import org.trakket.model.User;

import java.util.List;

public interface EventService<T extends Event> {
    T getEventById(Long id);
    List<T> getAllEvents();
    T saveEvent(T event);
    void deleteEvent(Long id);

    EventWatchStatus setWatchStatus(User user, Long eventId, WatchedStatus status);
}
