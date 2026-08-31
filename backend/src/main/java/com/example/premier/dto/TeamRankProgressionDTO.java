package com.example.premier.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TeamRankProgressionDTO {
    private Long teamId;
    private String teamName;
    private String season;
    private List<Point> points = new ArrayList<>();

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public List<Point> getPoints() { return points; }
    public void setPoints(List<Point> points) { this.points = points; }

    public static class Point {
        private Long snapshotId;
        private Integer roundNumber;
        private LocalDate snapshotDate;
        private Integer rank;        // null = team didn't play yet at this snapshot
        private Integer rankChange;  // 上一轮到本轮的变化, 正数=下降, 负数=上升; 第一轮为 null

        public Long getSnapshotId() { return snapshotId; }
        public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
        public Integer getRoundNumber() { return roundNumber; }
        public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }
        public LocalDate getSnapshotDate() { return snapshotDate; }
        public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }
        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
        public Integer getRankChange() { return rankChange; }
        public void setRankChange(Integer rankChange) { this.rankChange = rankChange; }
    }
}
