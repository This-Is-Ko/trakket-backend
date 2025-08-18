package org.sportstracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "football_event_watch_status")
public class FootballEventWatchStatus extends EventWatchStatus {

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private FootballEvent event;

}
