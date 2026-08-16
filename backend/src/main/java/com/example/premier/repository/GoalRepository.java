package com.example.premier.repository;

import com.example.premier.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByMatchId(Long matchId);
    void deleteByMatchId(Long matchId);
}