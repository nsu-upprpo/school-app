package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class GroupResponse {

    private final UUID id;
    private final UUID courseId;
    private final String courseName;
    private final UUID branchId;
    private final String branchName;
    private final UUID teacherId;
    private final String teacherName;
    private final String scheduleDescription;
    private final Integer maxStudents;
    private final int currentStudents;
    private final boolean active;

}
