package com.github.nsu_upprpo.school_app.model;

public class ProjectDto {
    private String id;
    private String groupId;
    private String name;
    private int totalLessons;
    private int maxScore;

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public int getMaxScore() {
        return maxScore;
    }
}
