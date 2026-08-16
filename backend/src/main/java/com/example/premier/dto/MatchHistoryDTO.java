package com.example.premier.dto;

import java.time.LocalDate;

public class MatchHistoryDTO {
    private Long matchId;
    private LocalDate matchDate;
    private String season;
    private String opponentName;
    private String homeAway;  // "HOME" / "AWAY"
    private Integer teamScore;
    private Integer opponentScore;
    private String result;    // "WIN" / "DRAW" / "LOSS"

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public LocalDate getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public String getOpponentName() { return opponentName; }
    public void setOpponentName(String opponentName) { this.opponentName = opponentName; }
    public String getHomeAway() { return homeAway; }
    public void setHomeAway(String homeAway) { this.homeAway = homeAway; }
    public Integer getTeamScore() { return teamScore; }
    public void setTeamScore(Integer teamScore) { this.teamScore = teamScore; }
    public Integer getOpponentScore() { return opponentScore; }
    public void setOpponentScore(Integer opponentScore) { this.opponentScore = opponentScore; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}