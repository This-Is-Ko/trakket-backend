package org.trakket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.trakket.enums.EventStatus;
import org.trakket.enums.ExternalMotorsportSource;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.model.MotorsportEvent;

import java.util.List;
import java.util.Optional;

public interface MotorsportEventRepository extends JpaRepository<MotorsportEvent, Long>, JpaSpecificationExecutor<MotorsportEvent> {

    Optional<MotorsportEvent> findByExternalSourceAndExternalSourceId(
            ExternalMotorsportSource externalSource, Long externalSourceId
    );

    Optional<MotorsportEvent> findByExternalSourceAndRaceName(
            ExternalMotorsportSource externalSource, String raceName
    );

    Optional<MotorsportEvent> findByCompetitionAndSeasonAndRound(
            MotorsportCompetition competition, int season, int round
    );

    @Query("SELECT COUNT(me) FROM MotorsportEvent me WHERE me.season = :season")
    Long countRacesInSeason(@Param("season") Integer season);

    @Query("SELECT COUNT(me) FROM MotorsportEvent me WHERE me.season = :season AND me.competition = :competition")
    Long countRacesInSeasonForCompetition(@Param("season") Integer season, @Param("competition") MotorsportCompetition competition);

    @Query("SELECT me FROM MotorsportEvent me WHERE me.status IN :statuses ORDER BY me.dateTime DESC")
    List<MotorsportEvent> findRecentByStatusIn(@Param("statuses") List<EventStatus> statuses);
}
