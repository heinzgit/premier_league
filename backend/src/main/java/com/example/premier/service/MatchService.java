package com.example.premier.service;

import com.example.premier.dto.GoalDTO;
import com.example.premier.dto.MatchDTO;
import com.example.premier.entity.Goal;
import com.example.premier.entity.Match;
import com.example.premier.entity.Team;
import com.example.premier.repository.GoalRepository;
import com.example.premier.repository.MatchRepository;
import com.example.premier.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final GoalRepository goalRepository;
    private final TeamRepository teamRepository;

    public MatchService(MatchRepository matchRepository,
                        GoalRepository goalRepository,
                        TeamRepository teamRepository) {
        this.matchRepository = matchRepository;
        this.goalRepository = goalRepository;
        this.teamRepository = teamRepository;
    }

    public List<MatchDTO> findAll() {
        return matchRepository.findAll().stream().map(this::toDTO).toList();
    }

    public List<MatchDTO> findBySeason(String season) {
        if (season == null || season.isBlank()) return findAll();
        return matchRepository.findBySeasonOrderByMatchDateDesc(season).stream().map(this::toDTO).toList();
    }

    public Optional<MatchDTO> findById(Long id) {
        return matchRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public MatchDTO create(MatchDTO dto) {
        validateTeams(dto);
        Match m = new Match();
        m.setHomeTeamId(dto.getHomeTeamId());
        m.setAwayTeamId(dto.getAwayTeamId());
        m.setHomeScore(dto.getHomeScore() == null ? 0 : dto.getHomeScore());
        m.setAwayScore(dto.getAwayScore() == null ? 0 : dto.getAwayScore());
        m.setMatchDate(dto.getMatchDate());
        m.setSeason(dto.getSeason());
        Match saved = matchRepository.save(m);
        if (dto.getGoals() != null) {
            for (GoalDTO g : dto.getGoals()) {
                Goal goal = new Goal();
                goal.setMatchId(saved.getId());
                goal.setTeamId(g.getTeamId());
                goal.setScorerName(g.getScorerName());
                goal.setMinute(g.getMinute());
                goal.setGoalType(g.getGoalType() == null ? "REGULAR" : g.getGoalType());
                goalRepository.save(goal);
            }
        }
        return toDTO(saved);
    }

    @Transactional
    public Optional<MatchDTO> update(Long id, MatchDTO dto) {
        return matchRepository.findById(id).map(m -> {
            validateTeams(dto);
            m.setHomeTeamId(dto.getHomeTeamId());
            m.setAwayTeamId(dto.getAwayTeamId());
            m.setHomeScore(dto.getHomeScore() == null ? 0 : dto.getHomeScore());
            m.setAwayScore(dto.getAwayScore() == null ? 0 : dto.getAwayScore());
            m.setMatchDate(dto.getMatchDate());
            m.setSeason(dto.getSeason());
            Match saved = matchRepository.save(m);
            goalRepository.deleteByMatchId(saved.getId());
            if (dto.getGoals() != null) {
                for (GoalDTO g : dto.getGoals()) {
                    Goal goal = new Goal();
                    goal.setMatchId(saved.getId());
                    goal.setTeamId(g.getTeamId());
                    goal.setScorerName(g.getScorerName());
                    goal.setMinute(g.getMinute());
                    goal.setGoalType(g.getGoalType() == null ? "REGULAR" : g.getGoalType());
                    goalRepository.save(goal);
                }
            }
            return toDTO(saved);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!matchRepository.existsById(id)) return false;
        goalRepository.deleteByMatchId(id);
        matchRepository.deleteById(id);
        return true;
    }

    private void validateTeams(MatchDTO dto) {
        if (dto.getHomeTeamId() == null || dto.getAwayTeamId() == null) {
            throw new IllegalArgumentException("必须指定主队和客队");
        }
        if (dto.getHomeTeamId().equals(dto.getAwayTeamId())) {
            throw new IllegalArgumentException("主队和客队不能是同一支球队");
        }
        if (dto.getMatchDate() == null) throw new IllegalArgumentException("比赛日期不能为空");
        if (dto.getSeason() == null || dto.getSeason().isBlank()) throw new IllegalArgumentException("赛季不能为空");
        if (!teamRepository.existsById(dto.getHomeTeamId())) throw new IllegalArgumentException("主队不存在");
        if (!teamRepository.existsById(dto.getAwayTeamId())) throw new IllegalArgumentException("客队不存在");
    }

    public MatchDTO toDTO(Match m) {
        MatchDTO d = new MatchDTO();
        d.setId(m.getId());
        d.setHomeTeamId(m.getHomeTeamId());
        d.setAwayTeamId(m.getAwayTeamId());
        d.setHomeScore(m.getHomeScore());
        d.setAwayScore(m.getAwayScore());
        d.setMatchDate(m.getMatchDate());
        d.setSeason(m.getSeason());

        Map<Long, Team> teamCache = new HashMap<>();
        teamRepository.findById(m.getHomeTeamId()).ifPresent(t -> teamCache.put(t.getId(), t));
        teamRepository.findById(m.getAwayTeamId()).ifPresent(t -> teamCache.put(t.getId(), t));

        d.setHomeTeamName(teamCache.containsKey(m.getHomeTeamId()) ? teamCache.get(m.getHomeTeamId()).getName() : null);
        d.setAwayTeamName(teamCache.containsKey(m.getAwayTeamId()) ? teamCache.get(m.getAwayTeamId()).getName() : null);

        for (Goal g : goalRepository.findByMatchId(m.getId())) {
            GoalDTO gd = new GoalDTO();
            gd.setId(g.getId());
            gd.setMatchId(g.getMatchId());
            gd.setTeamId(g.getTeamId());
            gd.setScorerName(g.getScorerName());
            gd.setMinute(g.getMinute());
            gd.setGoalType(g.getGoalType());
            teamRepository.findById(g.getTeamId()).ifPresent(t -> gd.setTeamName(t.getName()));
            d.getGoals().add(gd);
        }
        return d;
    }
}