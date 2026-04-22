package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateLessonRequest {
    private UUID projectId;

    @NotNull(message = "Группа обязательна")
    private UUID groupId;

    @NotNull(message = "Время начала обязательно")
    private LocalDateTime startTime;

    @NotNull(message = "Время окончания обязательно")
    private LocalDateTime endTime;

    private String topic;
}
