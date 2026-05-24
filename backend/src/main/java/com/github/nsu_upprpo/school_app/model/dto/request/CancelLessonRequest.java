package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelLessonRequest {

    @Size(max = 255, message = "Причина не должна превышать 255 символов")
    private String reason;
}

