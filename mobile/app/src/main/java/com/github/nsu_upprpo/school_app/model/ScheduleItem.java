package com.github.nsu_upprpo.school_app.model;

public class ScheduleItem {
    private final String title;
    private final String subtitle;
    private final String time;
    private final int stripeColorResId;

    public ScheduleItem(String title, String subtitle, String time, int stripeColorResId) {
        this.title = title;
        this.subtitle = subtitle;
        this.time = time;
        this.stripeColorResId = stripeColorResId;
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
