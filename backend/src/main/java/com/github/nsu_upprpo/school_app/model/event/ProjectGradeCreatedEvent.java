package com.github.nsu_upprpo.school_app.model.event;

import java.util.UUID;

public record ProjectGradeCreatedEvent(
        UUID gradeId,
        UUID projectId,
        UUID childId,
        UUID teacherId,
        Integer score
) {
}
