package org.sportstracker.repository;

import org.sportstracker.enums.WatchedStatus;
import org.sportstracker.model.MotorsportEvent;
import org.sportstracker.model.MotorsportEventWatchStatus;
import org.sportstracker.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MotorsportEventWatchStatusRepository extends JpaRepository<MotorsportEventWatchStatus, Long> {
    Optional<MotorsportEventWatchStatus> findByEventAndUser(MotorsportEvent event, User user);
    List<MotorsportEventWatchStatus> findAllByUser(User user);

    List<MotorsportEventWatchStatus> findByUserAndStatusNotOrderByEvent_DateTimeDesc(
            User user,
            WatchedStatus status,
            Pageable pageable
    );

    @Query("""
           select e.competition as competition, count(distinct e.id) as cnt
           from MotorsportEventWatchStatus ws
           join ws.event e
           where ws.user = :user and ws.status <> :excluded
           group by e.competition
           """)
    List<CompetitionCount> countWatchedByCompetition(@Param("user") User user,
                                                                                        @Param("excluded") WatchedStatus excluded);
}
