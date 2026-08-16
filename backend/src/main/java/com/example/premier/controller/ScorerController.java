package com.example.premier.controller;

import com.example.premier.dto.ScorerDTO;
import com.example.premier.service.ScorerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scorers")
public class ScorerController {
    private final ScorerService scorerService;

    public ScorerController(ScorerService scorerService) {
        this.scorerService = scorerService;
    }

    @GetMapping
    public List<ScorerDTO> scorers(@RequestParam(required = false) String season) {
        return scorerService.computeScorers(season);
    }
}