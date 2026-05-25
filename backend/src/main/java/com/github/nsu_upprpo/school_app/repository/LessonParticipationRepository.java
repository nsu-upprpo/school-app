package com.github.nsu_upprpo.school_app.repository;

import com.github.nsu_upprpo.school_app.model.entity.LessonParticipation;
import com.github.nsu_upprpo.school_app.model.entity.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonParticipationRepository extends JpaRepository<LessonParticipation, UUID> {

    List<LessonParticipation> findByLessonId(UUID lessonId);

    List<LessonParticipation> findByChildId(UUID childId);

    Optional<LessonParticipation> findByLessonIdAndChildId(UUID lessonId, UUID childId);

    boolean existsByLessonIdAndChildId(UUID lessonId, UUID childId);

    @Query("""
            select p from LessonParticipation p
            where p.child.id = :childId
              and p.lesson.startTime between :from and :to
            """)
    List<LessonParticipation> findByChildIdAndPeriod(
            @Param("childId") UUID childId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
            select count(p)
            from LessonParticipation p
            where p.child.id = :childId
              and p.lesson.project.id = :projectId
              and p.status = :status
            """)
    long countByChildProjectAndStatus(
            @Param("childId") UUID childId,
            @Param("projectId") UUID projectId,
            @Param("status") ParticipationStatus status
    );

    default long countVisitedLessonsByChildAndProject(UUID childId, UUID projectId) {
        return countByChildProjectAndStatus(childId, projectId, ParticipationStatus.PRESENT);
    }
}
