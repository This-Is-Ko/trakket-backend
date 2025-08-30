package org.trakket.model;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.trakket.enums.EventStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    protected LocalDateTime dateTime;

    protected Integer round;

    @Column(length = 200)
    protected String location;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    protected EventStatus status;

    @Column(name = "external_link")
    protected String externalLink;

    @Column(name = "last_updated")
    protected LocalDateTime lastUpdated;

    abstract String getTitle();

    abstract String getSubtitle();
}