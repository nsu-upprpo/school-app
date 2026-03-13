package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCourseRequest {

    @NotBlank(message = "Название курса обязательно")
    @Size(max = 100)
    private String name;

    private String description;

    private Integer minAge;

    private Integer maxAge;

}
