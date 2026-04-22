package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class LessonResponse {

    private final UUID id;
    private final UUID projectId;
    private final UUID groupId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String topic;
    private final String status;
    private final String cancelReason;

}
