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
    List<FootballCompetitionCount> countWatchedByCompetition(@Param("user") User user,
                                                             @Param("excluded") WatchedStatus excluded);

    // Count of watched events by home team
    @Query("""
        SELECT fe.homeTeam.id AS teamId, fe.homeTeam.name AS teamName, COUNT(ws) AS cnt
        FROM FootballEventWatchStatus ws
        JOIN ws.event fe
        WHERE ws.user = :user AND ws.status <> :excluded
        GROUP BY fe.homeTeam.id, fe.homeTeam.name
        """)
    List<TeamCount> countWatchedHomeTeams(@Param("user") User user, @Param("excluded") WatchedStatus excluded);

    // Count of watched events by away team
    @Query("""
        SELECT fe.awayTeam.id AS teamId, fe.awayTeam.name AS teamName, COUNT(ws) AS cnt
        FROM FootballEventWatchStatus ws
        JOIN ws.event fe
        WHERE ws.user = :user AND ws.status <> :excluded
        GROUP BY fe.awayTeam.id, fe.awayTeam.name
        """)
    List<TeamCount> countWatchedAwayTeams(@Param("user") User user, @Param("excluded") WatchedStatus excluded);

    // Count watched matches for a given team in a given season (season type may be String or integer in your model)
    @Query("""
        SELECT COUNT(ws)
        FROM FootballEventWatchStatus ws
        JOIN ws.event fe
        WHERE ws.user = :user AND ws.status <> :excluded
          AND (fe.homeTeam.id = :teamId OR fe.awayTeam.id = :teamId)
        """)
    Long countWatchedMatchesForTeam(
            @Param("user") User user,
            @Param("teamId") Long teamId,
            @Param("excluded") WatchedStatus excluded
    );

    // Count of watched events (helper)
    @Query("SELECT COUNT(ws) FROM FootballEventWatchStatus ws WHERE ws.user = :user AND ws.status <> :excluded")
    Long countWatchedByUser(@Param("user") User user, @Param("excluded") WatchedStatus excluded);

}
