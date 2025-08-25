package org.sportstracker.model;

import jakarta.persistence.*;
import lombok.Data;
import org.sportstracker.enums.WatchedStatus;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class EventWatchStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WatchedStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    protected LocalDateTime updatedDateTime;
}
