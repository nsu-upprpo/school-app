package com.github.nsu_upprpo.school_app.model;

public class LessonDto {
    private String id;
    private String groupId;
    private String projectId;
    private String startTime;
    private String endTime;
    private String status;
    private String topic;
    private String cancelReason;

    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getProjectId() { return projectId; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public String getTopic() { return topic; }
    public String getCancelReason() { return cancelReason; }
}
