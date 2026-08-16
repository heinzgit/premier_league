package com.example.premier.dto;

public class GoalDTO {
    private Long id;
    private Long matchId;
    private Long teamId;
    private String teamName;
    private String scorerName;
    private Integer minute;
    private String goalType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getScorerName() { return scorerName; }
    public void setScorerName(String scorerName) { this.scorerName = scorerName; }
    public Integer getMinute() { return minute; }
    public void setMinute(Integer minute) { this.minute = minute; }
    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }
}