package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RescheduleLessonRequest {

    @NotNull(message = "ID целевого занятия обязателен")
    private UUID targetLessonId;

    @Size(max = 255, message = "Причина не должна превышать 255 символов")
    private String reason;
}
