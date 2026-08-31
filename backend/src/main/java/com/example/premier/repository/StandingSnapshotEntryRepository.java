package com.example.premier.repository;

import com.example.premier.entity.StandingSnapshotEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StandingSnapshotEntryRepository extends JpaRepository<StandingSnapshotEntry, Long> {
    List<StandingSnapshotEntry> findBySnapshotIdOrderByRankPositionAsc(Long snapshotId);
    List<StandingSnapshotEntry> findByTeamIdAndSnapshotIdIn(Long teamId, List<Long> snapshotIds);
}
