package org.sportstracker.repository;

import org.sportstracker.model.FootballEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FootballEventRepository extends JpaRepository<FootballEvent, Long> {
}
