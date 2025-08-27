package org.sportstracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.sportstracker.enums.ExternalFootballSource;
import org.sportstracker.enums.FootballCompetition;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "football_events")
public class FootballEvent extends Event {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private FootballCompetition competition;

    @Column(nullable = false, length = 100)
    private String homeTeam;

    @Column(nullable = false, length = 100)
    private String awayTeam;

    @Column
    private Integer homeScore;

    @Column
    private Integer awayScore;

    @Enumerated(EnumType.STRING)
    protected ExternalFootballSource externalSource;

    protected Long externalSourceId;

    @Override
    public String getTitle() {
        return homeTeam + " vs " + awayTeam;
    }

    @Override
    public String getSubtitle() {
        return String.format("Round " + round);
    }
}
