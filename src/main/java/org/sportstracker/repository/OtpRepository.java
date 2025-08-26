package org.sportstracker.repository;

import org.sportstracker.model.Otp;
import org.sportstracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    void deleteByUser(User user);
    Optional<Otp> findByUser(User user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Otp o set o.attempts = o.attempts + 1 where o.id = :id")
    void incrementAttempts(@Param("id") Long id);
}
