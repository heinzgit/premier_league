package com.example.premier.controller;

import com.example.premier.dto.SeasonSquadDTO;
import com.example.premier.service.SeasonSquadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SeasonSquadController {
    private final SeasonSquadService seasonSquadService;

    public SeasonSquadController(SeasonSquadService seasonSquadService) {
        this.seasonSquadService = seasonSquadService;
    }

    @GetMapping("/teams/{teamId}/squad")
    public List<SeasonSquadDTO> teamSquad(@PathVariable Long teamId,
                                          @RequestParam String season) {
        return seasonSquadService.getTeamSquad(teamId, season);
    }

    @GetMapping("/season-squads")
    public List<SeasonSquadDTO> seasonSquads(@RequestParam String season) {
        return seasonSquadService.getSeasonSquads(season);
    }
}