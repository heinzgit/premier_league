package com.example.premier.service;

import com.example.premier.dto.TeamDTO;
import com.example.premier.entity.Team;
import com.example.premier.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<TeamDTO> findAll() {
        return teamRepository.findAll().stream().map(this::toDTO).toList();
    }

    public Optional<TeamDTO> findById(Long id) {
        return teamRepository.findById(id).map(this::toDTO);
    }

    public TeamDTO create(TeamDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("球队名称不能为空");
        }
        if (!teamRepository.findByNameIgnoreCase(dto.getName().trim()).isEmpty()) {
            throw new IllegalArgumentException("球队名称已存在");
        }
        Team t = new Team();
        t.setName(dto.getName().trim());
        t.setShortName(dto.getShortName());
        t.setFoundedYear(dto.getFoundedYear());
        t.setStadium(dto.getStadium());
        t.setCity(dto.getCity());
        return toDTO(teamRepository.save(t));
    }

    public Optional<TeamDTO> update(Long id, TeamDTO dto) {
        return teamRepository.findById(id).map(t -> {
            if (dto.getName() != null && !dto.getName().isBlank()) t.setName(dto.getName().trim());
            t.setShortName(dto.getShortName());
            t.setFoundedYear(dto.getFoundedYear());
            t.setStadium(dto.getStadium());
            t.setCity(dto.getCity());
            return toDTO(teamRepository.save(t));
        });
    }

    public boolean delete(Long id) {
        if (!teamRepository.existsById(id)) return false;
        teamRepository.deleteById(id);
        return true;
    }

    private TeamDTO toDTO(Team t) {
        TeamDTO d = new TeamDTO();
        d.setId(t.getId());
        d.setName(t.getName());
        d.setShortName(t.getShortName());
        d.setFoundedYear(t.getFoundedYear());
        d.setStadium(t.getStadium());
        d.setCity(t.getCity());
        return d;
    }
}