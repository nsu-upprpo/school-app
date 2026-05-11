package com.github.nsu_upprpo.school_app.model.event;

import java.util.UUID;

public record AttendanceMarkedEvent(
        UUID attendanceId,
        UUID lessonId,
        UUID childId,
        UUID teacherId,
        String status
) {
}
