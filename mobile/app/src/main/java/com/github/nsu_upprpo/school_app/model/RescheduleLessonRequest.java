package com.github.nsu_upprpo.school_app.model;

public class RescheduleLessonRequest {
    private String targetLessonId;
    private String reason;

    public RescheduleLessonRequest(String targetLessonId, String reason) {
        this.targetLessonId = targetLessonId;
        this.reason = reason;
    }
}
