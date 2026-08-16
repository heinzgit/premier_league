package com.example.premier.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lineup_players")
public class LineupPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lineup_id", nullable = false)
    private Long lineupId;

    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;

    @Column(length = 20)
    private String position;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "pos_x")
    private Integer posX;

    @Column(name = "pos_y")
    private Integer posY;

    @Column(name = "shirt_number", length = 10)
    private String shirtNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLineupId() { return lineupId; }
    public void setLineupId(Long lineupId) { this.lineupId = lineupId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public Integer getPosX() { return posX; }
    public void setPosX(Integer posX) { this.posX = posX; }
    public Integer getPosY() { return posY; }
    public void setPosY(Integer posY) { this.posY = posY; }
    public String getShirtNumber() { return shirtNumber; }
    public void setShirtNumber(String shirtNumber) { this.shirtNumber = shirtNumber; }
}