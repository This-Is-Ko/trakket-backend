package org.trakket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.trakket.enums.ExternalFootballSource;
import org.trakket.enums.FootballCompetition;

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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private FootballTeam homeTeam;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private FootballTeam awayTeam;

    @Column
    private Integer homeScore;

    @Column
    private Integer awayScore;

    @Enumerated(EnumType.STRING)
    private ExternalFootballSource externalSource;

    private Long externalSourceId;

    @Override
    public String getTitle() {
        return homeTeam.getName() + " vs " + awayTeam.getName();
    }

    @Override
    public String getSubtitle() {
        return String.format("Week %s", round);
    }
}
