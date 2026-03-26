package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateGroupRequest {

    @NotBlank(message = "Название группы обязательно")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Курс обязателен")
    private UUID courseId;

    @NotNull(message = "Филиал обязателен")
    private UUID branchId;

    @NotNull(message = "Преподаватель обязателен")
    private UUID teacherId;

    @Size(max = 255)
    private String scheduleDescription;

    private Integer maxStudents;

}
