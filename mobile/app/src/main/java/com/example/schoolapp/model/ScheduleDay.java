package com.example.schoolapp.model;

import java.util.List;

public class ScheduleDay {
    private final String dayTitle;
    private final List<ScheduleItem> items;

    public ScheduleDay(String dayTitle, List<ScheduleItem> items) {
        this.dayTitle = dayTitle;
        this.items = items;
    }

    public String getDayTitle() {
        return dayTitle;
    }

    public List<ScheduleItem> getItems() {
        return items;
    }
}
