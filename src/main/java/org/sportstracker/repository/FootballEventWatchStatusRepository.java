package org.sportstracker.repository;

import org.sportstracker.model.FootballEvent;
import org.sportstracker.model.FootballEventWatchStatus;
import org.sportstracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FootballEventWatchStatusRepository extends JpaRepository<FootballEventWatchStatus, Long> {

    Optional<FootballEventWatchStatus> findByEventAndUser(FootballEvent event, User user);
}
