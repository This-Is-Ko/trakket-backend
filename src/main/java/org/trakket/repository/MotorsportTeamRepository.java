package org.trakket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.trakket.model.MotorsportTeam;

import java.util.Optional;

public interface MotorsportTeamRepository extends JpaRepository<MotorsportTeam, Long> {

    Optional<MotorsportTeam> findByName(String name);

    boolean existsByName(String name);

}
