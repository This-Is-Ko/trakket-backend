package org.trakket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.trakket.model.FootballTeam;

import java.util.List;
import java.util.Optional;

public interface FootballTeamRepository extends JpaRepository<FootballTeam, Long> {

    Optional<FootballTeam> findByName(String name);

    boolean existsByName(String name);

    @Query(value = "SELECT * FROM football_teams t " +
            "WHERE LOWER(:name) = LOWER(t.name) " +
            "   OR LOWER(:name) = ANY(t.alternative_names)",
            nativeQuery = true)
    Optional<FootballTeam> findByNameOrAlternative(@Param("name") String name);

    Optional<FootballTeam> findBySofascoreExternalId(Long sofascoreExternalId);

    // Return in alphabetical order on name
    List<FootballTeam> findAllByOrderByNameAsc();

}
