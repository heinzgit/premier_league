package com.example.premier.dto;

import java.util.List;

public class MatchMatrixDTO {
    private List<String> teams;           // 行/列球队名,顺序对应
    private List<List<Cell>> cells;       // cells[i][j]: 行 i 球队 vs 列 j 球队

    public static class Cell {
        // 行主 vs 列客
        private Integer homeScore;        // 行球队的比分 (行主身份)
        private Integer awayScore;        // 列球队的比分
        private boolean homePlayed;       // 这场主客场比赛是否已进行

        // 行客 vs 列主
        private Integer homeScore2;       // 列球队的比分 (列主身份)
        private Integer awayScore2;       // 行球队的比分 (行客身份)
        private boolean awayPlayed;

        public Integer getHomeScore() { return homeScore; }
        public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }
        public Integer getAwayScore() { return awayScore; }
        public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }
        public boolean isHomePlayed() { return homePlayed; }
        public void setHomePlayed(boolean homePlayed) { this.homePlayed = homePlayed; }
        public Integer getHomeScore2() { return homeScore2; }
        public void setHomeScore2(Integer homeScore2) { this.homeScore2 = homeScore2; }
        public Integer getAwayScore2() { return awayScore2; }
        public void setAwayScore2(Integer awayScore2) { this.awayScore2 = awayScore2; }
        public boolean isAwayPlayed() { return awayPlayed; }
        public void setAwayPlayed(boolean awayPlayed) { this.awayPlayed = awayPlayed; }
    }

    public List<String> getTeams() { return teams; }
    public void setTeams(List<String> teams) { this.teams = teams; }
    public List<List<Cell>> getCells() { return cells; }
    public void setCells(List<List<Cell>> cells) { this.cells = cells; }
}