package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Builder
public class AttendanceResponse {

    private final UUID id;
    private final UUID lessonId;
    private final UUID rescheduledLessonId;
    private final UUID childId;
    private final String childName;
    private final UUID markedById;
    private final LocalDateTime markedAt;
    private final String status;

}
