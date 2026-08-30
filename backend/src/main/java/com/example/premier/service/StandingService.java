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

        map.values().forEach(s -> s.setGoalDifference(s.getGoalsFor() - s.getGoalsAgainst()));

        // 相互战绩:只计入与同积分/净胜球/进球数完全相同的对手之间的比赛积分
        Map<Long, Integer> h2hPoints = new HashMap<>();
        for (Match m : matches) {
            StandingDTO home = map.get(m.getHomeTeamId());
            StandingDTO away = map.get(m.getAwayTeamId());
            if (home.getPoints() != away.getPoints()
                    || home.getGoalDifference() != away.getGoalDifference()
                    || home.getGoalsFor() != away.getGoalsFor()) {
                continue;
            }
            if (m.getHomeScore() > m.getAwayScore()) {
                h2hPoints.merge(m.getHomeTeamId(), 3, Integer::sum);
            } else if (m.getHomeScore() < m.getAwayScore()) {
                h2hPoints.merge(m.getAwayTeamId(), 3, Integer::sum);
            } else {
                h2hPoints.merge(m.getHomeTeamId(), 1, Integer::sum);
                h2hPoints.merge(m.getAwayTeamId(), 1, Integer::sum);
            }
        }

        return map.values().stream()
                .sorted(Comparator
                        .comparingInt(StandingDTO::getPoints).reversed()
                        .thenComparing(Comparator.comparingInt(StandingDTO::getGoalDifference).reversed())
                        .thenComparing(Comparator.comparingInt(StandingDTO::getGoalsFor).reversed())
                        .thenComparing(Comparator.comparingInt(
                                (StandingDTO s) -> h2hPoints.getOrDefault(s.getTeamId(), 0)).reversed())
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