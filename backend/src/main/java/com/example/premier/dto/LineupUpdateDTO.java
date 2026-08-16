package com.example.premier.dto;

public class LineupUpdateDTO {
    private LineupDTO home;
    private LineupDTO away;

    public LineupDTO getHome() { return home; }
    public void setHome(LineupDTO home) { this.home = home; }
    public LineupDTO getAway() { return away; }
    public void setAway(LineupDTO away) { this.away = away; }
}