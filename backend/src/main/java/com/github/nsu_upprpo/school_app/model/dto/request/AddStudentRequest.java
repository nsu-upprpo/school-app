package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddStudentRequest {

    @NotNull(message = "ID ребёнка обязателен")
    private UUID childId;

}
