package com.example.premier.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "scorer_name", nullable = false, length = 100)
    private String scorerName;

    private Integer minute;

    @Column(name = "goal_type", length = 20)
    private String goalType = "REGULAR";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getScorerName() { return scorerName; }
    public void setScorerName(String scorerName) { this.scorerName = scorerName; }
    public Integer getMinute() { return minute; }
    public void setMinute(Integer minute) { this.minute = minute; }
    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }
}