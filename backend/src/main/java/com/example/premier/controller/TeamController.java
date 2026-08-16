package com.example.premier.controller;

import com.example.premier.dto.TeamDTO;
import com.example.premier.repository.MatchRepository;
import com.example.premier.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;
    private final MatchRepository matchRepository;

    public TeamController(TeamService teamService, MatchRepository matchRepository) {
        this.teamService = teamService;
        this.matchRepository = matchRepository;
    }

    @GetMapping
    public List<TeamDTO> all() { return teamService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> one(@PathVariable Long id) {
        return teamService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TeamDTO> create(@RequestBody TeamDTO dto) {
        try {
            return ResponseEntity.ok(teamService.create(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDTO> update(@PathVariable Long id, @RequestBody TeamDTO dto) {
        try {
            return teamService.update(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!teamService.findById(id).isPresent()) return ResponseEntity.notFound().build();
        if (matchRepository.countByAnyTeam(id) > 0) {
            return ResponseEntity.status(409).build();
        }
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}