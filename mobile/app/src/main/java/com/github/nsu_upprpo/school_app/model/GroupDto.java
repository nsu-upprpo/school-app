package com.github.nsu_upprpo.school_app.model;

public class GroupDto {
    private boolean active;
    private String branchId;
    private String branchName;
    private String courseId;
    private String courseName;
    private int currentStudents;

    private String id;
    private String groupId;
    private String groupName;

    private int maxStudents;
    private String scheduleDescription;
    private String teacherId;
    private String teacherName;

    public boolean isActive() {
        return active;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCurrentStudents() {
        return currentStudents;
    }

    public String getId() {
        return id;
    }

    public String getGroupId() {
        if (groupId != null && !groupId.isEmpty()) {
            return groupId;
        }
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public String getScheduleDescription() {
        return scheduleDescription;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }
}