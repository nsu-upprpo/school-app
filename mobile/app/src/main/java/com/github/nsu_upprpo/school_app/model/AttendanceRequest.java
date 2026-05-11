package com.github.nsu_upprpo.school_app.model;

public class AttendanceRequest {
    private String childId;
    private String status;

    public AttendanceRequest(String childId, String status) {
        this.childId = childId;
        this.status = status;
    }
}