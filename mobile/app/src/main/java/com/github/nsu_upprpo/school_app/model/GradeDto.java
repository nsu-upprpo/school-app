package com.github.nsu_upprpo.school_app.model;

public class GradeDto {
    private String id;
    private String projectId;
    private String childId;
    private String childName;
    private Integer score;
    private String comment;

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getChildId() {
        return childId;
    }

    public String getChildName() {
        return childName;
    }

    public Integer getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }
}
