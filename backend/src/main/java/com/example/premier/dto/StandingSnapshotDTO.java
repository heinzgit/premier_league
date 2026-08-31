package com.example.premier.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StandingSnapshotDTO {
    private Long id;
    private String season;
    private Integer roundNumber;
    private LocalDate snapshotDate;
    private String note;
    private List<Entry> entries = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }
    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<Entry> getEntries() { return entries; }
    public void setEntries(List<Entry> entries) { this.entries = entries; }

    public static class Entry {
        private Long teamId;
        private String teamName;
        private Integer rankPosition;

        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        public Integer getRankPosition() { return rankPosition; }
        public void setRankPosition(Integer rankPosition) { this.rankPosition = rankPosition; }
    }
}
