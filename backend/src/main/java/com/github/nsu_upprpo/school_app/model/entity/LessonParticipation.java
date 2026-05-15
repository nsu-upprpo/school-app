package com.github.nsu_upprpo.school_app.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "lesson_participations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_participation_lesson_child",
                columnNames = {"lesson_id", "child_id"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LessonParticipation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ParticipationStatus status;

    /** Куда перенесли (для {@link ParticipationStatus#RESCHEDULED_OUT}). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduled_to_lesson_id")
    private Lesson rescheduledTo;

    /** Откуда перенесли (для {@link ParticipationStatus#RESCHEDULED_IN}). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduled_from_lesson_id")
    private Lesson rescheduledFrom;

    @Column(length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}

