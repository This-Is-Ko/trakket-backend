package org.sportstracker.service;

import org.sportstracker.model.EventWatchStatus;
import org.sportstracker.repository.EventWatchStatusRepository;
import org.springframework.stereotype.Service;

@Service
public class EventWatchStatusService {

    private final EventWatchStatusRepository repository;

    public EventWatchStatusService(EventWatchStatusRepository repository) {
        this.repository = repository;
    }

    public EventWatchStatus trackStatus(EventWatchStatus status) {
        return repository.save(status);
    }
}
