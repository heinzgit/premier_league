package com.example.premier.repository;

import com.example.premier.entity.StandingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StandingSnapshotRepository extends JpaRepository<StandingSnapshot, Long> {
    List<StandingSnapshot> findBySeasonOrderByRoundNumberAsc(String season);
    Optional<StandingSnapshot> findBySeasonAndRoundNumber(String season, Integer roundNumber);
}
