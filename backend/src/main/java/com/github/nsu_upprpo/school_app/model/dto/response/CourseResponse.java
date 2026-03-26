package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CourseResponse {

    private final UUID id;
    private final String name;
    private final String description;
    private final Integer minAge;
    private final Integer maxAge;
    private final boolean active;

}
