package org.trakket.repository;

import org.trakket.enums.ExternalFootballSource;
import org.trakket.model.FootballEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FootballEventRepository extends JpaRepository<FootballEvent, Long>, JpaSpecificationExecutor<FootballEvent> {

    Optional<FootballEvent> findByExternalSourceAndExternalSourceId(
            ExternalFootballSource externalSource, Long externalSourceId
    );

}
