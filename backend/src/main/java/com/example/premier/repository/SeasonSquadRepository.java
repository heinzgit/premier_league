package com.example.premier.repository;

import com.example.premier.entity.SeasonSquad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeasonSquadRepository extends JpaRepository<SeasonSquad, Long> {
    List<SeasonSquad> findByTeamIdAndSeasonOrderByPlayerName(Long teamId, String season);

    List<SeasonSquad> findBySeasonOrderByTeamIdAscPlayerNameAsc(String season);

    @Modifying
    @Query("DELETE FROM SeasonSquad s WHERE s.season = :season AND s.teamId = :teamId")
    void deleteBySeasonAndTeam(@Param("season") String season, @Param("teamId") Long teamId);
}