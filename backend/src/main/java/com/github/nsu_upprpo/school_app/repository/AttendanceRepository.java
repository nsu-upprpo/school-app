package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByLessonId(UUID lessonId);
    List<Attendance> findByChildId(UUID childId);
    boolean existsByLessonIdAndChildId(UUID lessonId, UUID childId);
}
