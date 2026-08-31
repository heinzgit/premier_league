package com.example.premier.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "standing_snapshots",
       uniqueConstraints = @UniqueConstraint(columnNames = {"season", "round_number"}))
public class StandingSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String season;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(length = 200)
    private String note;

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
}
