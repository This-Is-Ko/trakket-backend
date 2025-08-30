package org.trakket.model;

public class TennisEvent extends Event {
    private String playerOne;
    private String playerTwo;
    private String score;

    @Override
    public String getTitle() {
        return playerOne + " vs " + playerTwo;
    }

    @Override
    public String getSubtitle() {
        return "Round " + round;
    }
}
