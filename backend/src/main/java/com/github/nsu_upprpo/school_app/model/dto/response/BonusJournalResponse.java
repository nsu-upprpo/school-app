package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class BonusJournalResponse {

    private UUID projectId;
    private String projectName;

    private UUID groupId;
    private String courseName;

    private int visitedLessons;
    private int totalLessons;
    private int missedLessons;

    private Integer score;
    private Integer maxScore;
    private String gradeComment;

}