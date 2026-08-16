package com.example.premier.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "season_squads")
public class SeasonSquad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String season;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;

    @Column(name = "shirt_number", length = 10)
    private String shirtNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getShirtNumber() { return shirtNumber; }
    public void setShirtNumber(String shirtNumber) { this.shirtNumber = shirtNumber; }
}