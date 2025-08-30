package org.trakket.repository;

import org.trakket.enums.WatchedStatus;
import org.trakket.model.FootballEvent;
import org.trakket.model.FootballEventWatchStatus;
import org.trakket.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FootballEventWatchStatusRepository extends JpaRepository<FootballEventWatchStatus, Long> {

    Optional<FootballEventWatchStatus> findByEventAndUser(FootballEvent event, User user);

    List<FootballEventWatchStatus> findAllByUser(User user);

    List<FootballEventWatchStatus> findByUserAndStatusNotOrderByEvent_DateTimeDesc(
            User user,
            WatchedStatus status,
            Pageable pageable
    );

    List<FootballEventWatchStatus> findByUserAndEventIdIn(User user, List<Long> eventIds);

    @Query("""
           select e.competition as competition, count(distinct e.id) as cnt
           from FootballEventWatchStatus ws
           join ws.event e
           where ws.user = :user and ws.status <> :excluded
           group by e.competition
           """)
    List<CompetitionCount> countWatchedByCompetition(@Param("user") User user,
                                                     @Param("excluded") WatchedStatus excluded);

}
