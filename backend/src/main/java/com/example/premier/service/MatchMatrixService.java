package com.example.premier.service;

import com.example.premier.dto.MatchMatrixDTO;
import com.example.premier.dto.StandingDTO;
import com.example.premier.entity.Match;
import com.example.premier.entity.Team;
import com.example.premier.repository.MatchRepository;
import com.example.premier.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchMatrixService {
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final StandingService standingService;

    public MatchMatrixService(TeamRepository teamRepository,
                              MatchRepository matchRepository,
                              StandingService standingService) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.standingService = standingService;
    }

    public MatchMatrixDTO build(String season) {
        List<Match> matches = (season == null || season.isBlank())
                ? matchRepository.findAll()
                : matchRepository.findBySeasonOrderByMatchDateDesc(season);

        // 用积分榜的顺序排球队 (积分 > 净胜球 > 进球数 > 队名)
        // computeStandings 已只包含该赛季参赛球队,自动处理升降级
        List<StandingDTO> standings = standingService.computeStandings(season);

        Map<Long, Team> teamById = new HashMap<>();
        for (Team t : teamRepository.findAll()) teamById.put(t.getId(), t);

        List<Long> orderedIds = new ArrayList<>();
        for (StandingDTO s : standings) orderedIds.add(s.getTeamId());
        // 若有比赛但积分榜未覆盖(极端情况),按队名补齐
        for (Match m : matches) {
            if (!orderedIds.contains(m.getHomeTeamId())) orderedIds.add(m.getHomeTeamId());
            if (!orderedIds.contains(m.getAwayTeamId())) orderedIds.add(m.getAwayTeamId());
        }

        List<Team> teams = new ArrayList<>();
        for (Long id : orderedIds) teams.add(teamById.get(id));

        Map<Long, Integer> indexByTeamId = new HashMap<>();
        for (int i = 0; i < teams.size(); i++) {
            indexByTeamId.put(teams.get(i).getId(), i);
        }

        MatchMatrixDTO dto = new MatchMatrixDTO();
        dto.setTeams(teams.stream().map(Team::getName).toList());

        int n = teams.size();
        List<List<MatchMatrixDTO.Cell>> rows = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            List<MatchMatrixDTO.Cell> row = new ArrayList<>(n);
            for (int j = 0; j < n; j++) row.add(new MatchMatrixDTO.Cell());
            rows.add(row);
        }

        for (Match m : matches) {
            Integer rowIdx = indexByTeamId.get(m.getHomeTeamId());
            Integer colIdx = indexByTeamId.get(m.getAwayTeamId());
            if (rowIdx == null || colIdx == null) continue;

            MatchMatrixDTO.Cell cell = rows.get(rowIdx).get(colIdx);
            cell.setHomePlayed(true);
            cell.setHomeScore(m.getHomeScore());
            cell.setAwayScore(m.getAwayScore());

            MatchMatrixDTO.Cell mirror = rows.get(colIdx).get(rowIdx);
            mirror.setAwayPlayed(true);
            mirror.setHomeScore2(m.getHomeScore());
            mirror.setAwayScore2(m.getAwayScore());
        }

        dto.setCells(rows);
        return dto;
    }
}