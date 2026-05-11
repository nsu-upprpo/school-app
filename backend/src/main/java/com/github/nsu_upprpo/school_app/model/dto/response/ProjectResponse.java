package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ProjectResponse {

    private final UUID id;
    private final UUID groupId;
    private final String name;
    private final Integer totalLessons;
    private final Integer maxScore;
    private final String status;

}
