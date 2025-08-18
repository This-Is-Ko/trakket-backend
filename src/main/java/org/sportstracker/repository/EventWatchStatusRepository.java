package org.sportstracker.repository;

import org.sportstracker.model.EventWatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventWatchStatusRepository extends JpaRepository<EventWatchStatus, Long> {
}
