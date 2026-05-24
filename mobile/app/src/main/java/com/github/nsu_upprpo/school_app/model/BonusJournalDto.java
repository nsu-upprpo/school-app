package com.github.nsu_upprpo.school_app.model;

public class BonusJournalDto {
    private String projectId;
    private String projectName;
    private String groupId;
    private String courseName;
    private int visitedLessons;
    private int totalLessons;
    private int missedLessons;
    private Integer score;
    private Integer maxScore;
    private String gradeComment;

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getVisitedLessons() {
        return visitedLessons;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public int getMissedLessons() {
        return missedLessons;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public String getGradeComment() {
        return gradeComment;
    }
}
