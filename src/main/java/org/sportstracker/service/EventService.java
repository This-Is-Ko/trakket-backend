package org.sportstracker.service;

import org.sportstracker.model.Event;

import java.util.List;

public interface EventService<T extends Event> {
    T getEventById(Long id);
    List<T> getAllEvents();
    T saveEvent(T event);
    void deleteEvent(Long id);
}
