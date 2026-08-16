package com.example.premier.repository;

import com.example.premier.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findBySeasonOrderByMatchDateDesc(String season);

    @Query("SELECT m FROM Match m WHERE (m.homeTeamId = :teamId OR m.awayTeamId = :teamId) ORDER BY m.matchDate DESC")
    List<Match> findMatchesByTeam(@Param("teamId") Long teamId);

    @Query("SELECT m FROM Match m WHERE m.homeTeamId = :teamId OR m.awayTeamId = :teamId")
    List<Match> findAllByAnyTeam(@Param("teamId") Long teamId);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.homeTeamId = :teamId OR m.awayTeamId = :teamId")
    long countByAnyTeam(@Param("teamId") Long teamId);

    @Query("SELECT m FROM Match m WHERE m.season = :season AND (m.homeTeamId = :teamId OR m.awayTeamId = :teamId) ORDER BY m.matchDate DESC")
    List<Match> findBySeasonAndTeam(@Param("season") String season, @Param("teamId") Long teamId);
}