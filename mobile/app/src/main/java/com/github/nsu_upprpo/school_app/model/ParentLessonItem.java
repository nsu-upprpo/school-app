package com.github.nsu_upprpo.school_app.model;

import java.io.Serializable;

public class ParentLessonItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String lessonId;
    private String childId;
    private String groupId;
    private String courseName;
    private String teacherName;
    private String topic;
    private String childNames;
    private String startTime;
    private String endTime;
    private long dateMillis;
    private String status;
    private String childStatus;
    private String rescheduledFromLessonId;
    private String rescheduledToLessonId;

    public ParentLessonItem(String lessonId, String childId, String groupId, String courseName,
                            String teacherName, String topic, String childNames,
                            String startTime, String endTime, long dateMillis,
                            String status, String childStatus,
                            String rescheduledFromLessonId, String rescheduledToLessonId) {
        this.lessonId = lessonId;
        this.childId = childId;
        this.groupId = groupId;
        this.courseName = courseName;
        this.teacherName = teacherName;
        this.topic = topic;
        this.childNames = childNames;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dateMillis = dateMillis;
        this.status = status;
        this.childStatus = childStatus;
        this.rescheduledFromLessonId = rescheduledFromLessonId;
        this.rescheduledToLessonId = rescheduledToLessonId;
    }

    public String getLessonId() { return lessonId; }
    public String getChildId() { return childId; }
    public String getGroupId() { return groupId; }
    public String getCourseName() { return courseName; }
    public String getTeacherName() { return teacherName; }
    public String getTopic() { return topic; }
    public String getChildNames() { return childNames; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public long getDateMillis() { return dateMillis; }
    public String getStatus() { return status; }
    public String getChildStatus() { return childStatus; }
    public String getRescheduledFromLessonId() { return rescheduledFromLessonId; }
    public String getRescheduledToLessonId() { return rescheduledToLessonId; }
}
