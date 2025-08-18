package org.sportstracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sportstracker.enums.EventStatus;

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

    @Column(length = 100)
    protected String competition;

    @Column(length = 200)
    protected String location;

    @Column(nullable = false)
    protected EventStatus status;

    @Column(name = "external_link")
    protected String externalLink;

    public abstract String getSummary();
}