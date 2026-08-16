package com.example.premier.repository;

import com.example.premier.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query("SELECT t FROM Team t WHERE LOWER(t.name) = LOWER(:name)")
    List<Team> findByNameIgnoreCase(@Param("name") String name);
}