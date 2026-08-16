package com.example.premier.dto;

public class StandingDTO {
    private Long teamId;
    private String teamName;
    private int played;
    private int won;
    private int drawn;
    private int lost;
    private int goalsFor;
    private int goalsAgainst;
    private int goalDifference;
    private int points;

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public int getPlayed() { return played; }
    public void setPlayed(int played) { this.played = played; }
    public int getWon() { return won; }
    public void setWon(int won) { this.won = won; }
    public int getDrawn() { return drawn; }
    public void setDrawn(int drawn) { this.drawn = drawn; }
    public int getLost() { return lost; }
    public void setLost(int lost) { this.lost = lost; }
    public int getGoalsFor() { return goalsFor; }
    public void setGoalsFor(int goalsFor) { this.goalsFor = goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }
    public void setGoalsAgainst(int goalsAgainst) { this.goalsAgainst = goalsAgainst; }
    public int getGoalDifference() { return goalDifference; }
    public void setGoalDifference(int goalDifference) { this.goalDifference = goalDifference; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}