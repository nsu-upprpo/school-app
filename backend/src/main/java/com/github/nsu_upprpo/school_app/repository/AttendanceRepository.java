package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByLessonId(UUID lessonId);
    List<Attendance> findByChildId(UUID childId);
    boolean existsByLessonIdAndChildId(UUID lessonId, UUID childId);

    @Query("""
        select count(a)
        from Attendance a
        where a.child.id = :childId
          and a.lesson.project.id = :projectId
          and a.status = 'PRESENT'
    """)
    long countVisitedLessonsByChildAndProject(
            @Param("childId") UUID childId,
            @Param("projectId") UUID projectId
    );

}
