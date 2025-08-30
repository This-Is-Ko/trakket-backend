package org.trakket.service;

import org.trakket.model.EventWatchStatus;
import org.trakket.repository.EventWatchStatusRepository;
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
