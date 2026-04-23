package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ProjectGradeResponse {

    private final UUID id;
    private final UUID projectId;
    private final UUID childId;
    private final String childName;
    private final UUID teacherId;
    private final Integer score;
    private final String comment;

}
