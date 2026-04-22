package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class MarkAttendanceRequest {

    @NotNull(message = "ID ученика обязателен")
    private UUID childId;

    @NotBlank(message = "Статус обязателен")
    private String status;

}
