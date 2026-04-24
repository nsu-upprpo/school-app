package com.github.nsu_upprpo.school_app.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class ScheduleStorage {

    private static final String PREFS = "schedule_prefs";

    private static final String KEY_TEACHER_SCHEDULE_JSON = "teacher_schedule_json";
    private static final String KEY_TEACHER_TODAY_MODE = "teacher_today_mode";

    private final SharedPreferences prefs;

    public ScheduleStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveTeacherScheduleJson(String json) {
        prefs.edit().putString(KEY_TEACHER_SCHEDULE_JSON, json).apply();
    }

    public String getTeacherScheduleJson() {
        return prefs.getString(KEY_TEACHER_SCHEDULE_JSON, null);
    }

    public boolean hasTeacherSchedule() {
        String json = getTeacherScheduleJson();
        return json != null && !json.isEmpty();
    }

    public void setTeacherTodayMode(boolean isTodayMode) {
        prefs.edit().putBoolean(KEY_TEACHER_TODAY_MODE, isTodayMode).apply();
    }

    public boolean isTeacherTodayMode() {
        return prefs.getBoolean(KEY_TEACHER_TODAY_MODE, true);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}