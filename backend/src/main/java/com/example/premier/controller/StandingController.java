package com.example.premier.controller;

import com.example.premier.dto.StandingDTO;
import com.example.premier.service.StandingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/standings")
public class StandingController {
    private final StandingService standingService;

    public StandingController(StandingService standingService) {
        this.standingService = standingService;
    }

    @GetMapping
    public List<StandingDTO> standings(@RequestParam(required = false) String season) {
        return standingService.computeStandings(season);
    }
}