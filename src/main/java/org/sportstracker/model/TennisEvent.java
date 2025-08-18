package org.sportstracker.model;

public class TennisEvent extends Event {
    private String playerOne;
    private String playerTwo;
    private String score;

    @Override
    public String getSummary() {
        return playerOne + " vs " + playerTwo + " (" + competition + ")";
    }
}
