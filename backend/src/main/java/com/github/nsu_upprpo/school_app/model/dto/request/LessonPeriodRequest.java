package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LessonPeriodRequest {

    @NotNull(message = "Начальная дата обязательна")
    LocalDateTime fromDate;

    @NotNull(message = "Конечная дата обязательна")
    LocalDateTime toDate;
}
