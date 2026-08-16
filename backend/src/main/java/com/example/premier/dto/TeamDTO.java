package com.example.premier.dto;

public class TeamDTO {
    private Long id;
    private String name;
    private String shortName;
    private Integer foundedYear;
    private String stadium;
    private String city;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public Integer getFoundedYear() { return foundedYear; }
    public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }
    public String getStadium() { return stadium; }
    public void setStadium(String stadium) { this.stadium = stadium; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}