package com.example.premier.service;

import com.example.premier.dto.MatchHistoryDTO;
import com.example.premier.entity.Match;
import com.example.premier.entity.Team;
import com.example.premier.repository.MatchRepository;
import com.example.premier.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TeamHistoryService {
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    public TeamHistoryService(MatchRepository matchRepository, TeamRepository teamRepository) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
    }

    public List<MatchHistoryDTO> historyOf(Long teamId) {
        Optional<Team> teamOpt = teamRepository.findById(teamId);
        if (teamOpt.isEmpty()) return List.of();
        List<Match> matches = matchRepository.findMatchesByTeam(teamId);
        List<MatchHistoryDTO> result = new ArrayList<>();
        for (Match m : matches) {
            MatchHistoryDTO h = new MatchHistoryDTO();
            h.setMatchId(m.getId());
            h.setMatchDate(m.getMatchDate());
            h.setSeason(m.getSeason());
            boolean isHome = m.getHomeTeamId().equals(teamId);
            if (isHome) {
                h.setHomeAway("HOME");
                h.setTeamScore(m.getHomeScore());
                h.setOpponentScore(m.getAwayScore());
                teamRepository.findById(m.getAwayTeamId()).ifPresent(t -> h.setOpponentName(t.getName()));
                if (m.getHomeScore() > m.getAwayScore()) h.setResult("WIN");
                else if (m.getHomeScore() < m.getAwayScore()) h.setResult("LOSS");
                else h.setResult("DRAW");
            } else {
                h.setHomeAway("AWAY");
                h.setTeamScore(m.getAwayScore());
                h.setOpponentScore(m.getHomeScore());
                teamRepository.findById(m.getHomeTeamId()).ifPresent(t -> h.setOpponentName(t.getName()));
                if (m.getAwayScore() > m.getHomeScore()) h.setResult("WIN");
                else if (m.getAwayScore() < m.getHomeScore()) h.setResult("LOSS");
                else h.setResult("DRAW");
            }
            result.add(h);
        }
        result.sort(Comparator.comparing(MatchHistoryDTO::getMatchDate).reversed());
        return result;
    }
}