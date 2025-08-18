package org.sportstracker.repository;

import org.sportstracker.model.EventComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, Long> {
}
