package com.example.premier.controller;

import com.example.premier.dto.StandingSnapshotDTO;
import com.example.premier.dto.TeamRankProgressionDTO;
import com.example.premier.service.StandingSnapshotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/standing-snapshots")
public class StandingSnapshotController {
    private final StandingSnapshotService service;

    public StandingSnapshotController(StandingSnapshotService service) {
        this.service = service;
    }

    @GetMapping
    public List<StandingSnapshotDTO> list(@RequestParam String season) {
        return service.listBySeason(season);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandingSnapshotDTO> get(@PathVariable Long id) {
        return service.getSnapshot(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequest req) {
        try {
            StandingSnapshotDTO dto = service.createSnapshot(
                    req.season, req.roundNumber, req.date, req.note);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.deleteSnapshot(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/teams/{teamId}/progression")
    public TeamRankProgressionDTO progression(@PathVariable Long teamId,
                                              @RequestParam String season) {
        return service.getTeamProgression(teamId, season);
    }

    public static class CreateRequest {
        public String season;
        public Integer roundNumber;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        public LocalDate date;
        public String note;
    }
}
