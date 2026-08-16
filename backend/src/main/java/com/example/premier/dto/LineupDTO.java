package com.example.premier.dto;

import java.util.ArrayList;
import java.util.List;

public class LineupDTO {
    private Long id;
    private Long matchId;
    private Long teamId;
    private String teamName;
    private String formation;
    private List<LineupPlayerDTO> players = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }
    public List<LineupPlayerDTO> getPlayers() { return players; }
    public void setPlayers(List<LineupPlayerDTO> players) { this.players = players; }
}