package com.example.premier.service;

import com.example.premier.dto.LineupDTO;
import com.example.premier.dto.LineupPlayerDTO;
import com.example.premier.dto.LineupUpdateDTO;
import com.example.premier.entity.Lineup;
import com.example.premier.entity.LineupPlayer;
import com.example.premier.entity.Match;
import com.example.premier.repository.LineupPlayerRepository;
import com.example.premier.repository.LineupRepository;
import com.example.premier.repository.MatchRepository;
import com.example.premier.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LineupService {
    private final LineupRepository lineupRepository;
    private final LineupPlayerRepository lineupPlayerRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final SeasonSquadService seasonSquadService;

    public LineupService(LineupRepository lineupRepository,
                         LineupPlayerRepository lineupPlayerRepository,
                         MatchRepository matchRepository,
                         TeamRepository teamRepository,
                         SeasonSquadService seasonSquadService) {
        this.lineupRepository = lineupRepository;
        this.lineupPlayerRepository = lineupPlayerRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.seasonSquadService = seasonSquadService;
    }

    public LineupUpdateDTO getLineups(Long matchId) {
        LineupUpdateDTO dto = new LineupUpdateDTO();
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isEmpty()) return dto;
        Match match = matchOpt.get();
        dto.setHome(findAndBuild(matchId, match.getHomeTeamId()));
        dto.setAway(findAndBuild(matchId, match.getAwayTeamId()));
        return dto;
    }

    private LineupDTO findAndBuild(Long matchId, Long teamId) {
        LineupDTO dto = new LineupDTO();
        dto.setMatchId(matchId);
        dto.setTeamId(teamId);
        teamRepository.findById(teamId).ifPresent(t -> dto.setTeamName(t.getName()));
        lineupRepository.findByMatchIdAndTeamId(matchId, teamId).ifPresent(l -> {
            dto.setId(l.getId());
            dto.setFormation(l.getFormation());
            for (LineupPlayer p : lineupPlayerRepository.findByLineupIdOrderByDisplayOrderAsc(l.getId())) {
                LineupPlayerDTO pd = new LineupPlayerDTO();
                pd.setId(p.getId());
                pd.setPlayerName(p.getPlayerName());
                pd.setPosition(p.getPosition());
                pd.setDisplayOrder(p.getDisplayOrder());
                pd.setPosX(p.getPosX());
                pd.setPosY(p.getPosY());
                pd.setShirtNumber(p.getShirtNumber());
                dto.getPlayers().add(pd);
            }
        });
        return dto;
    }

    @Transactional
    public LineupUpdateDTO save(Long matchId, LineupUpdateDTO payload) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isEmpty()) throw new IllegalArgumentException("比赛不存在");
        Match match = matchOpt.get();

        if (payload.getHome() != null) upsert(matchId, match.getHomeTeamId(), payload.getHome());
        if (payload.getAway() != null) upsert(matchId, match.getAwayTeamId(), payload.getAway());

        // 保存阵容后,刷新受影响的 (season, team) 阵容快照
        seasonSquadService.rebuild(match.getSeason(), match.getHomeTeamId());
        seasonSquadService.rebuild(match.getSeason(), match.getAwayTeamId());

        return getLineups(matchId);
    }

    private void upsert(Long matchId, Long expectedTeamId, LineupDTO dto) {
        if (dto.getTeamId() != null && !dto.getTeamId().equals(expectedTeamId)) {
            throw new IllegalArgumentException("阵容球队与比赛队伍不一致");
        }
        Lineup lineup = lineupRepository.findByMatchIdAndTeamId(matchId, expectedTeamId)
                .orElseGet(() -> {
                    Lineup l = new Lineup();
                    l.setMatchId(matchId);
                    l.setTeamId(expectedTeamId);
                    return l;
                });
        lineup.setFormation(dto.getFormation());
        Lineup saved = lineupRepository.save(lineup);

        // 全量替换球员
        lineupPlayerRepository.deleteByLineupId(saved.getId());
        if (dto.getPlayers() != null) {
            int order = 1;
            for (LineupPlayerDTO pd : dto.getPlayers()) {
                if (pd.getPlayerName() == null || pd.getPlayerName().isBlank()) continue;
                LineupPlayer p = new LineupPlayer();
                p.setLineupId(saved.getId());
                p.setPlayerName(pd.getPlayerName().trim());
                p.setPosition(pd.getPosition());
                p.setDisplayOrder(pd.getDisplayOrder() != null ? pd.getDisplayOrder() : order++);
                p.setPosX(pd.getPosX());
                p.setPosY(pd.getPosY());
                p.setShirtNumber(pd.getShirtNumber());
                lineupPlayerRepository.save(p);
            }
        }
    }
}