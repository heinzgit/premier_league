package com.example.premier.repository;

import com.example.premier.entity.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LineupRepository extends JpaRepository<Lineup, Long> {
    List<Lineup> findByMatchId(Long matchId);
    Optional<Lineup> findByMatchIdAndTeamId(Long matchId, Long teamId);
    void deleteByMatchId(Long matchId);
}