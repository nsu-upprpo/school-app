package com.github.nsu_upprpo.school_app.model;

public class ParentLessonItem {
    private String lessonId;
    private String groupId;
    private String courseName;
    private String teacherName;
    private String topic;
    private String childNames;
    private String startTime;
    private String endTime;
    private long dateMillis;
    private String status;

    public ParentLessonItem(String lessonId, String groupId, String courseName,
                            String teacherName, String topic, String childNames,
                            String startTime, String endTime, long dateMillis,
                            String status) {
        this.lessonId = lessonId;
        this.groupId = groupId;
        this.courseName = courseName;
        this.teacherName = teacherName;
        this.topic = topic;
        this.childNames = childNames;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dateMillis = dateMillis;
        this.status = status;
    }

    public String getLessonId() { return lessonId; }
    public String getGroupId() { return groupId; }
    public String getCourseName() { return courseName; }
    public String getTeacherName() { return teacherName; }
    public String getTopic() { return topic; }
    public String getChildNames() { return childNames; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public long getDateMillis() { return dateMillis; }
    public String getStatus() { return status; }
}