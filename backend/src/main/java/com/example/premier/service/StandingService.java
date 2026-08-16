package com.example.premier.service;

import com.example.premier.dto.StandingDTO;
import com.example.premier.entity.Match;
import com.example.premier.repository.MatchRepository;
import com.example.premier.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StandingService {
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    public StandingService(TeamRepository teamRepository, MatchRepository matchRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    public List<StandingDTO> computeStandings(String season) {
        List<Match> matches = (season == null || season.isBlank())
                ? matchRepository.findAll()
                : matchRepository.findBySeasonOrderByMatchDateDesc(season);

        // 只初始化本赛季有出场的球队 (升降级:非该赛季球队不出现在榜单)
        Map<Long, StandingDTO> map = new HashMap<>();
        for (Match m : matches) {
            ensureStanding(map, m.getHomeTeamId());
            ensureStanding(map, m.getAwayTeamId());
        }

        for (Match m : matches) {
            StandingDTO home = map.get(m.getHomeTeamId());
            StandingDTO away = map.get(m.getAwayTeamId());

            home.setPlayed(home.getPlayed() + 1);
            away.setPlayed(away.getPlayed() + 1);
            home.setGoalsFor(home.getGoalsFor() + m.getHomeScore());
            home.setGoalsAgainst(home.getGoalsAgainst() + m.getAwayScore());
            away.setGoalsFor(away.getGoalsFor() + m.getAwayScore());
            away.setGoalsAgainst(away.getGoalsAgainst() + m.getHomeScore());

            if (m.getHomeScore() > m.getAwayScore()) {
                home.setWon(home.getWon() + 1); home.setPoints(home.getPoints() + 3);
                away.setLost(away.getLost() + 1);
            } else if (m.getHomeScore() < m.getAwayScore()) {
                away.setWon(away.getWon() + 1); away.setPoints(away.getPoints() + 3);
                home.setLost(home.getLost() + 1);
            } else {
                home.setDrawn(home.getDrawn() + 1); home.setPoints(home.getPoints() + 1);
                away.setDrawn(away.getDrawn() + 1); away.setPoints(away.getPoints() + 1);
            }
        }

        return map.values().stream()
                .peek(s -> s.setGoalDifference(s.getGoalsFor() - s.getGoalsAgainst()))
                .sorted(Comparator
                        .comparingInt(StandingDTO::getPoints).reversed()
                        .thenComparingInt(StandingDTO::getGoalDifference).reversed()
                        .thenComparingInt(StandingDTO::getGoalsFor).reversed()
                        .thenComparing(StandingDTO::getTeamName))
                .toList();
    }

    private void ensureStanding(Map<Long, StandingDTO> map, Long teamId) {
        if (map.containsKey(teamId)) return;
        StandingDTO s = new StandingDTO();
        s.setTeamId(teamId);
        teamRepository.findById(teamId).ifPresent(t -> s.setTeamName(t.getName()));
        map.put(teamId, s);
    }
}