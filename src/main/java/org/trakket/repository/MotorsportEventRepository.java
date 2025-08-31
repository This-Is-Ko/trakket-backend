package org.trakket.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.trakket.enums.ExternalFootballSource;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.model.MotorsportEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MotorsportEventRepository extends JpaRepository<MotorsportEvent, Long>, JpaSpecificationExecutor<MotorsportEvent> {

    Optional<MotorsportEvent> findByExternalSourceAndExternalSourceId(
            ExternalFootballSource externalSource, Long externalSourceId
    );

    Optional<MotorsportEvent> findByCompetitionAndSeasonAndRound(
            MotorsportCompetition competition, int season, int round
    );

    @Query("SELECT COUNT(me) FROM MotorsportEvent me WHERE me.season = :season")
    Long countRacesInSeason(@Param("season") Integer season);
}
