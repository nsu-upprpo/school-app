package com.github.nsu_upprpo.school_app.model;

public class LessonDto {
    private String id;
    private String lessonId;
    private String groupId;
    private String projectId;
    private String startTime;
    private String endTime;
    private String status;
    private String topic;
    private String teacherName;
    private String cancelReason;
    private String childStatus;
    private String rescheduledFromLessonId;
    private String rescheduledToLessonId;

    public String getId() {
        if (id != null && !id.isEmpty()) {
            return id;
        }

        return lessonId;
    }

    public String getLessonId() { return lessonId; }
    public String getGroupId() { return groupId; }
    public String getProjectId() { return projectId; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public String getTopic() { return topic; }
    public String getTeacherName() { return teacherName; }
    public String getCancelReason() { return cancelReason; }
    public String getChildStatus() { return childStatus; }
    public String getRescheduledFromLessonId() { return rescheduledFromLessonId; }
    public String getRescheduledToLessonId() { return rescheduledToLessonId; }
}
