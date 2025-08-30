package org.trakket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.trakket.enums.ExternalMotorsportSource;
import org.trakket.enums.MotorsportCompetition;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "motorsport_events",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"competition", "season", "round"})
        }
)
public class MotorsportEvent extends Event {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private MotorsportCompetition competition;

    private Integer season;

    @Column(length = 100)
    private String raceName;

    @Column(length = 100)
    private String circuitName;

    @Column(length = 100)
    private String winner;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "winner_team_id", nullable = false)
    private MotorsportTeam winnerTeam;

    @Enumerated(EnumType.STRING)
    private ExternalMotorsportSource externalSource;

    private Long externalSourceId;

    @Override
    public String getTitle() {
        return raceName;
    }

    @Override
    public String getSubtitle() {
        return "Round " + (round != null ? round : "-");
    }

    public void updateResult(MotorsportEvent other) {
        // Confirm the same race
        if (!this.competition.equals(other.getCompetition()) ||
                !this.season.equals(other.getSeason()) ||
                !this.round.equals(other.getRound())) {
            throw new IllegalArgumentException("Cannot update result for different race");
        }
        this.status = other.getStatus();
        this.winner = other.getWinner();
    }
}
