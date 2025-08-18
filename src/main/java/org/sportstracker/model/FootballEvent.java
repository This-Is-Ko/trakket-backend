package org.sportstracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "football_events")
public class FootballEvent extends Event {

    @Column(nullable = false, length = 100)
    private String homeTeam;

    @Column(nullable = false, length = 100)
    private String awayTeam;

    @Column
    private Integer homeScore;

    @Column
    private Integer awayScore;

    @Override
    public String getSummary() {
        return homeTeam + " vs " + awayTeam + " (" + competition + ")";
    }

}
