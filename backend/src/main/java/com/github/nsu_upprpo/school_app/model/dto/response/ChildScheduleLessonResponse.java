package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ChildScheduleLessonResponse {

    private final UUID lessonId;
    private final UUID groupId;
    private final UUID projectId;

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    private final String topic;

    /** Статус самого занятия (PLANNED/COMPLETED/CANCELLED). */
    private final String lessonStatus;

    /** Статус участия конкретного ребёнка (null = штатно). */
    private final String childStatus;

    private final UUID rescheduledToLessonId;

    private final UUID rescheduledFromLessonId;

    private final String reason;
}
