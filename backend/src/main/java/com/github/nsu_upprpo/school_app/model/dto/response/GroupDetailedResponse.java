package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class GroupDetailedResponse {

    private final UUID id;
    private final String courseName;
    private final String branchName;
    private final String teacherName;
    private final String scheduleDescription;
    private final Integer maxStudents;
    private final List<StudentInfo> students;

    @Getter
    @Builder
    public static class StudentInfo {
        private final UUID childId;
        private final String firstName;
        private final String lastName;
        private final LocalDate enrolledAt;
    }

}

