package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByGroupId(UUID groupId);
    List<Lesson> findByGroupIdOrderByStartTimeAsc(UUID groupId);
    List<Lesson> findByGroupIdAndStartTimeBetween(UUID groupId, LocalDateTime from, LocalDateTime to);
    List<Lesson> findByProjectId(UUID projectId);
}
