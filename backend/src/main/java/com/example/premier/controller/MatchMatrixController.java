package com.example.premier.controller;

import com.example.premier.dto.MatchMatrixDTO;
import com.example.premier.service.MatchMatrixService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matrix")
public class MatchMatrixController {
    private final MatchMatrixService service;

    public MatchMatrixController(MatchMatrixService service) {
        this.service = service;
    }

    @GetMapping
    public MatchMatrixDTO matrix(@RequestParam(required = false) String season) {
        return service.build(season);
    }
}