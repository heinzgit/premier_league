package com.example.premier.service;

import com.example.premier.dto.SeasonSquadDTO;
import com.example.premier.entity.Lineup;
import com.example.premier.entity.LineupPlayer;
import com.example.premier.entity.Match;
import com.example.premier.entity.SeasonSquad;
import com.example.premier.entity.Team;
import com.example.premier.repository.LineupPlayerRepository;
import com.example.premier.repository.LineupRepository;
import com.example.premier.repository.MatchRepository;
import com.example.premier.repository.SeasonSquadRepository;
import com.example.premier.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeasonSquadService {
    private final SeasonSquadRepository seasonSquadRepository;
    private final MatchRepository matchRepository;
    private final LineupRepository lineupRepository;
    private final LineupPlayerRepository lineupPlayerRepository;
    private final TeamRepository teamRepository;

    public SeasonSquadService(SeasonSquadRepository seasonSquadRepository,
                             MatchRepository matchRepository,
                             LineupRepository lineupRepository,
                             LineupPlayerRepository lineupPlayerRepository,
                             TeamRepository teamRepository) {
        this.seasonSquadRepository = seasonSquadRepository;
        this.matchRepository = matchRepository;
        this.lineupRepository = lineupRepository;
        this.lineupPlayerRepository = lineupPlayerRepository;
        this.teamRepository = teamRepository;
    }

    /**
     * 从 lineups 聚合某赛季某球队的阵容:每个球员取最近一场比赛的球衣号。
     * 调用前请确认 (season, teamId) 有效。
     */
    @Transactional
    public void rebuild(String season, Long teamId) {
        if (season == null || season.isBlank() || teamId == null) return;
        List<Match> matches = matchRepository.findBySeasonAndTeam(season, teamId);
        // playerName -> 收集 (matchDate, shirtNumber),最后按日期降序取首个
        Map<String, String> bestShirt = new LinkedHashMap<>();
        for (Match m : matches) {
            Lineup lineup = lineupRepository.findByMatchIdAndTeamId(m.getId(), teamId).orElse(null);
            if (lineup == null) continue;
            List<LineupPlayer> players = lineupPlayerRepository.findByLineupIdOrderByDisplayOrderAsc(lineup.getId());
            for (LineupPlayer p : players) {
                String name = p.getPlayerName();
                if (name == null || name.isBlank()) continue;
                bestShirt.putIfAbsent(name, p.getShirtNumber()); // matches 按 matchDate DESC 遍历,首次即最新
            }
        }

        seasonSquadRepository.deleteBySeasonAndTeam(season, teamId);
        bestShirt.forEach((name, shirt) -> {
            SeasonSquad s = new SeasonSquad();
            s.setSeason(season);
            s.setTeamId(teamId);
            s.setPlayerName(name);
            s.setShirtNumber(shirt);
            seasonSquadRepository.save(s);
        });
    }

    public List<SeasonSquadDTO> getTeamSquad(Long teamId, String season) {
        if (season == null || season.isBlank()) return List.of();
        List<SeasonSquad> rows = seasonSquadRepository.findByTeamIdAndSeasonOrderByPlayerName(teamId, season);
        String teamName = teamRepository.findById(teamId).map(Team::getName).orElse(null);
        return rows.stream().map(r -> {
            SeasonSquadDTO d = new SeasonSquadDTO();
            d.setId(r.getId());
            d.setSeason(r.getSeason());
            d.setTeamId(r.getTeamId());
            d.setTeamName(teamName);
            d.setPlayerName(r.getPlayerName());
            d.setShirtNumber(r.getShirtNumber());
            return d;
        }).toList();
    }

    public List<SeasonSquadDTO> getSeasonSquads(String season) {
        if (season == null || season.isBlank()) return List.of();
        List<SeasonSquad> rows = seasonSquadRepository.findBySeasonOrderByTeamIdAscPlayerNameAsc(season);
        Map<Long, String> teamNames = new HashMap<>();
        for (Team t : teamRepository.findAll()) teamNames.put(t.getId(), t.getName());
        return rows.stream().map(r -> {
            SeasonSquadDTO d = new SeasonSquadDTO();
            d.setId(r.getId());
            d.setSeason(r.getSeason());
            d.setTeamId(r.getTeamId());
            d.setTeamName(teamNames.get(r.getTeamId()));
            d.setPlayerName(r.getPlayerName());
            d.setShirtNumber(r.getShirtNumber());
            return d;
        }).toList();
    }
}