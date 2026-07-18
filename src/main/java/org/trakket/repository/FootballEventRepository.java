package org.trakket.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.trakket.enums.EventStatus;
import org.trakket.enums.ExternalFootballSource;
import org.trakket.model.FootballEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface FootballEventRepository extends JpaRepository<FootballEvent, Long>, JpaSpecificationExecutor<FootballEvent> {

    Optional<FootballEvent> findByExternalSourceAndExternalSourceId(
            ExternalFootballSource externalSource, Long externalSourceId
    );

    @Query("""
            SELECT COUNT(fe)
            FROM FootballEvent fe
            WHERE (fe.homeTeam.id = :teamId OR fe.awayTeam.id = :teamId)
            """)
    Long countMatchesForTeamInSeason(@Param("teamId") Long teamId);

    @Query("""
            SELECT COUNT(fe)
            FROM FootballEvent fe
            WHERE (fe.homeTeam.id = :teamId OR fe.awayTeam.id = :teamId)
            AND YEAR(fe.dateTime) = :season
            """)
    Long countMatchesForTeamInYear(@Param("teamId") Long teamId, @Param("season") int season);

    @Query("""
            SELECT DISTINCT YEAR(fe.dateTime)
            FROM FootballEvent fe
            WHERE fe.homeTeam.id = :teamId OR fe.awayTeam.id = :teamId
            ORDER BY YEAR(fe.dateTime)
            """)
    List<Integer> getSeasonsForTeam(@Param("teamId") Long teamId);

    @Query("SELECT fe FROM FootballEvent fe WHERE fe.status IN :statuses ORDER BY fe.dateTime DESC")
    List<FootballEvent> findRecentByStatusIn(@Param("statuses") List<EventStatus> statuses);
}
