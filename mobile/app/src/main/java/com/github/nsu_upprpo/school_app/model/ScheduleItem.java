package com.github.nsu_upprpo.school_app.model;

public class ScheduleItem {
    private String lessonId;
    private String groupId;
    private String title;
    private String subtitle;
    private String time;
    private int stripeColorResId;

    public ScheduleItem(String title, String subtitle, String time, int stripeColorResId) {
        this.title = title;
        this.subtitle = subtitle;
        this.time = time;
        this.stripeColorResId = stripeColorResId;
    }

    public ScheduleItem(String lessonId, String groupId, String title, String subtitle, String time, int stripeColorResId) {
        this.lessonId = lessonId;
        this.groupId = groupId;
        this.title = title;
        this.subtitle = subtitle;
        this.time = time;
        this.stripeColorResId = stripeColorResId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTime() {
        return time;
    }

    public int getStripeColorResId() {
        return stripeColorResId;
    }
}