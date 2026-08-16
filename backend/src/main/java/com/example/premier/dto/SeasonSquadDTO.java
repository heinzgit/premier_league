package com.example.premier.dto;

public class SeasonSquadDTO {
    private Long id;
    private String season;
    private Long teamId;
    private String teamName;
    private String playerName;
    private String shirtNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getShirtNumber() { return shirtNumber; }
    public void setShirtNumber(String shirtNumber) { this.shirtNumber = shirtNumber; }
}