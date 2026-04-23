package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateProjectRequest {
    @NotNull(message = "Группа обязательна")
    private UUID groupId;

    @NotBlank(message = "Название обязательно")
    private String name;

    private Integer totalLessons;
    private Integer maxScore;
}
