package org.trakket.repository;

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

}
