package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByBranchId(UUID branchId);
    List<Event> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime after);
    List<Event> findByBranchIdAndStartTimeAfterOrderByStartTimeAsc(UUID branchId, LocalDateTime after);
}

