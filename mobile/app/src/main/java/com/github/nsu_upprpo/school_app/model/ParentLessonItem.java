package com.github.nsu_upprpo.school_app.model;

public class ParentLessonItem {
    private String courseName;
    private String teacherName;
    private String startTime;
    private String endTime;
    private long dateMillis;

    public ParentLessonItem(String courseName, String teacherName,
                            String startTime, String endTime, long dateMillis) {
        this.courseName = courseName;
        this.teacherName = teacherName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dateMillis = dateMillis;
    }

    public String getCourseName() { return courseName; }
    public String getTeacherName() { return teacherName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public long getDateMillis() { return dateMillis; }
}