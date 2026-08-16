package com.example.premier.controller;

import com.example.premier.dto.MatchHistoryDTO;
import com.example.premier.service.TeamHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamHistoryController {
    private final TeamHistoryService teamHistoryService;

    public TeamHistoryController(TeamHistoryService teamHistoryService) {
        this.teamHistoryService = teamHistoryService;
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<MatchHistoryDTO>> history(@PathVariable Long id) {
        return ResponseEntity.ok(teamHistoryService.historyOf(id));
    }
}