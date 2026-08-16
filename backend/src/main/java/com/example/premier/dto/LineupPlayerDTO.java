package com.example.premier.dto;

public class LineupPlayerDTO {
    private Long id;
    private String playerName;
    private String position;
    private Integer displayOrder;
    private Integer posX;
    private Integer posY;
    private String shirtNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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