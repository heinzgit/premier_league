package com.example.premier.service;

import com.example.premier.dto.StandingDTO;
import com.example.premier.dto.StandingSnapshotDTO;
import com.example.premier.dto.TeamRankProgressionDTO;
import com.example.premier.entity.StandingSnapshot;
import com.example.premier.entity.StandingSnapshotEntry;
import com.example.premier.entity.Team;
import com.example.premier.repository.StandingSnapshotEntryRepository;
import com.example.premier.repository.StandingSnapshotRepository;
import com.example.premier.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StandingSnapshotService {
    private final StandingService standingService;
    private final StandingSnapshotRepository snapshotRepo;
    private final StandingSnapshotEntryRepository entryRepo;
    private final TeamRepository teamRepository;

    public StandingSnapshotService(StandingService standingService,
                                   StandingSnapshotRepository snapshotRepo,
                                   StandingSnapshotEntryRepository entryRepo,
                                   TeamRepository teamRepository) {
        this.standingService = standingService;
        this.snapshotRepo = snapshotRepo;
        this.entryRepo = entryRepo;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public StandingSnapshotDTO createSnapshot(String season, Integer roundNumber, LocalDate date, String note) {
        if (season == null || season.isBlank()) {
            throw new IllegalArgumentException("赛季不能为空");
        }
        if (roundNumber == null || roundNumber <= 0) {
            throw new IllegalArgumentException("轮次必须为正整数");
        }
        if (snapshotRepo.findBySeasonAndRoundNumber(season, roundNumber).isPresent()) {
            throw new IllegalArgumentException("该赛季第 " + roundNumber + " 轮快照已存在");
        }
        List<StandingDTO> standings = standingService.computeStandings(season);
        if (standings.isEmpty()) {
            throw new IllegalArgumentException("该赛季暂无比赛,无法保存快照");
        }

        StandingSnapshot snap = new StandingSnapshot();
        snap.setSeason(season);
        snap.setRoundNumber(roundNumber);
        snap.setSnapshotDate(date != null ? date : LocalDate.now());
        snap.setNote(note);
        StandingSnapshot saved = snapshotRepo.save(snap);

        int rank = 1;
        for (StandingDTO s : standings) {
            StandingSnapshotEntry e = new StandingSnapshotEntry();
            e.setSnapshotId(saved.getId());
            e.setTeamId(s.getTeamId());
            e.setRankPosition(rank++);
            entryRepo.save(e);
        }
        return toDTO(saved, standings);
    }

    public List<StandingSnapshotDTO> listBySeason(String season) {
        if (season == null || season.isBlank()) {
            throw new IllegalArgumentException("赛季不能为空");
        }
        List<StandingSnapshot> snaps = snapshotRepo.findBySeasonOrderByRoundNumberAsc(season);
        Map<Long, List<StandingDTO>> standingsByRound = new HashMap<>();
        // 取该赛季当前排名作为队伍名称参考
        List<StandingDTO> current = standingService.computeStandings(season);
        Map<Long, String> nameByTeam = new HashMap<>();
        for (StandingDTO s : current) nameByTeam.put(s.getTeamId(), s.getTeamName());

        List<StandingSnapshotDTO> result = new ArrayList<>();
        for (StandingSnapshot snap : snaps) {
            StandingSnapshotDTO dto = toDTO(snap, null);
            // 补 teamName
            for (StandingSnapshotEntry e : entryRepo.findBySnapshotIdOrderByRankPositionAsc(snap.getId())) {
                StandingSnapshotDTO.Entry ed = new StandingSnapshotDTO.Entry();
                ed.setTeamId(e.getTeamId());
                ed.setRankPosition(e.getRankPosition());
                ed.setTeamName(nameByTeam.getOrDefault(e.getTeamId(), "队伍#" + e.getTeamId()));
                dto.getEntries().add(ed);
            }
            result.add(dto);
        }
        return result;
    }

    public Optional<StandingSnapshotDTO> getSnapshot(Long id) {
        return snapshotRepo.findById(id).map(snap -> {
            StandingSnapshotDTO dto = toDTO(snap, null);
            List<StandingSnapshotEntry> entries = entryRepo.findBySnapshotIdOrderByRankPositionAsc(id);
            Map<Long, String> nameByTeam = new HashMap<>();
            for (StandingSnapshotEntry e : entries) {
                nameByTeam.computeIfAbsent(e.getTeamId(),
                        tid -> teamRepository.findById(tid).map(Team::getName).orElse("队伍#" + tid));
            }
            for (StandingSnapshotEntry e : entries) {
                StandingSnapshotDTO.Entry ed = new StandingSnapshotDTO.Entry();
                ed.setTeamId(e.getTeamId());
                ed.setTeamName(nameByTeam.get(e.getTeamId()));
                ed.setRankPosition(e.getRankPosition());
                dto.getEntries().add(ed);
            }
            return dto;
        });
    }

    @Transactional
    public boolean deleteSnapshot(Long id) {
        if (!snapshotRepo.existsById(id)) return false;
        snapshotRepo.deleteById(id);
        return true;
    }

    public TeamRankProgressionDTO getTeamProgression(Long teamId, String season) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("球队不存在"));

        TeamRankProgressionDTO dto = new TeamRankProgressionDTO();
        dto.setTeamId(team.getId());
        dto.setTeamName(team.getName());
        dto.setSeason(season);

        List<StandingSnapshot> snaps = snapshotRepo.findBySeasonOrderByRoundNumberAsc(season);
        List<Long> snapIds = snaps.stream().map(StandingSnapshot::getId).toList();
        Map<Long, Integer> rankBySnap = new HashMap<>();
        for (StandingSnapshotEntry e : entryRepo.findByTeamIdAndSnapshotIdIn(teamId, snapIds)) {
            rankBySnap.put(e.getSnapshotId(), e.getRankPosition());
        }

        Integer prevRank = null;
        for (StandingSnapshot snap : snaps) {
            TeamRankProgressionDTO.Point p = new TeamRankProgressionDTO.Point();
            p.setSnapshotId(snap.getId());
            p.setRoundNumber(snap.getRoundNumber());
            p.setSnapshotDate(snap.getSnapshotDate());
            Integer cur = rankBySnap.get(snap.getId());
            p.setRank(cur);  // null 表示该轮该队还未参赛
            if (cur != null && prevRank != null) {
                p.setRankChange(prevRank - cur);  // 负数 = 排名上升
            }
            dto.getPoints().add(p);
            if (cur != null) prevRank = cur;
        }
        return dto;
    }

    private StandingSnapshotDTO toDTO(StandingSnapshot snap, List<StandingDTO> standings) {
        StandingSnapshotDTO dto = new StandingSnapshotDTO();
        dto.setId(snap.getId());
        dto.setSeason(snap.getSeason());
        dto.setRoundNumber(snap.getRoundNumber());
        dto.setSnapshotDate(snap.getSnapshotDate());
        dto.setNote(snap.getNote());
        return dto;
    }
}
