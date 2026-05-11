package com.github.nsu_upprpo.school_app.model;

public class AttendanceDto {
    private String id;
    private String lessonId;
    private String childId;
    private String childName;
    private String status;
    private String markedAt;
    private String markedById;
    private String rescheduledLessonId;

    public String getId() { return id; }
    public String getLessonId() { return lessonId; }
    public String getChildId() { return childId; }
    public String getChildName() { return childName; }
    public String getStatus() { return status; }
    public String getMarkedAt() { return markedAt; }
    public String getMarkedById() { return markedById; }
    public String getRescheduledLessonId() { return rescheduledLessonId; }
}
