package com.example.premier.service;

import com.example.premier.dto.ScorerDTO;
import com.example.premier.entity.Goal;
import com.example.premier.entity.Match;
import com.example.premier.entity.Team;
import com.example.premier.repository.GoalRepository;
import com.example.premier.repository.MatchRepository;
import com.example.premier.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ScorerService {
    private final GoalRepository goalRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    public ScorerService(GoalRepository goalRepository,
                         MatchRepository matchRepository,
                         TeamRepository teamRepository) {
        this.goalRepository = goalRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
    }

    public List<ScorerDTO> computeScorers(String season) {
        List<Match> matches = (season == null || season.isBlank())
                ? matchRepository.findAll()
                : matchRepository.findBySeasonOrderByMatchDateDesc(season);
        if (matches.isEmpty()) return List.of();

        Map<Long, Goal> goalsById = new HashMap<>();
        for (Goal g : goalRepository.findAll()) {
            goalsById.put(g.getId(), g);
        }

        // 过滤属于该赛季的进球
        Map<String, Integer> counts = new HashMap<>();
        Map<String, Long> latestTeamId = new HashMap<>();
        for (Match m : matches) {
            for (Goal g : goalRepository.findByMatchId(m.getId())) {
                if ("OWN_GOAL".equalsIgnoreCase(g.getGoalType())) continue;
                String name = g.getScorerName().trim();
                counts.merge(name, 1, Integer::sum);
                // 最近一次同球员进球的球队 (此处按遍历顺序,后续按 id 倒序得到的是晚添加的)
                latestTeamId.put(name, g.getTeamId());
            }
        }

        Map<Long, Team> teams = new HashMap<>();
        for (Team t : teamRepository.findAll()) teams.put(t.getId(), t);

        return counts.entrySet().stream()
                .map(e -> new ScorerDTO(
                        e.getKey(),
                        teams.get(latestTeamId.get(e.getKey())) == null
                                ? null
                                : Objects.requireNonNull(teams.get(latestTeamId.get(e.getKey()))).getName(),
                        e.getValue()))
                .sorted(Comparator.comparingInt(ScorerDTO::getGoals).reversed()
                        .thenComparing(ScorerDTO::getScorerName))
                .toList();
    }
}