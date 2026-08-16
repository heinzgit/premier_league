package com.example.premier.controller;

import com.example.premier.dto.LineupUpdateDTO;
import com.example.premier.service.LineupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/matches/{matchId}/lineups")
public class LineupController {
    private final LineupService lineupService;

    public LineupController(LineupService lineupService) {
        this.lineupService = lineupService;
    }

    @GetMapping
    public LineupUpdateDTO get(@PathVariable Long matchId) {
        return lineupService.getLineups(matchId);
    }

    @PutMapping
    public ResponseEntity<?> save(@PathVariable Long matchId, @RequestBody LineupUpdateDTO dto) {
        try {
            return ResponseEntity.ok(lineupService.save(matchId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}