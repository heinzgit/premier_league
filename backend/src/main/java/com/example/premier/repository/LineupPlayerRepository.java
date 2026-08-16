package com.example.premier.repository;

import com.example.premier.entity.LineupPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LineupPlayerRepository extends JpaRepository<LineupPlayer, Long> {
    List<LineupPlayer> findByLineupIdOrderByDisplayOrderAsc(Long lineupId);
    void deleteByLineupId(Long lineupId);
}