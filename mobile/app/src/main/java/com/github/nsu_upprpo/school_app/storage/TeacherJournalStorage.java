package com.github.nsu_upprpo.school_app.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class TeacherJournalStorage {
    private static final String PREFS_NAME = "teacher_journal_storage";

    private static final String KEY_GROUPS_JSON = "teacher_groups_json";
    private static final String KEY_PROJECTS_PREFIX = "teacher_projects_";
    private static final String KEY_LESSONS_PREFIX = "teacher_lessons_";

    private final SharedPreferences prefs;

    public TeacherJournalStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean hasGroups() {
        String json = prefs.getString(KEY_GROUPS_JSON, "");
        return json != null && !json.isEmpty();
    }

    public String getGroupsJson() {
        return prefs.getString(KEY_GROUPS_JSON, "");
    }

    public void saveGroupsJson(String json) {
        prefs.edit().putString(KEY_GROUPS_JSON, json).apply();
    }

    public boolean hasProjectsForGroup(String groupId) {
        String json = prefs.getString(KEY_PROJECTS_PREFIX + groupId, "");
        return json != null && !json.isEmpty();
    }

    public String getProjectsJson(String groupId) {
        return prefs.getString(KEY_PROJECTS_PREFIX + groupId, "");
    }

    public void saveProjectsJson(String groupId, String json) {
        prefs.edit().putString(KEY_PROJECTS_PREFIX + groupId, json).apply();
    }

    public boolean hasLessonsForGroup(String groupId) {
        String json = prefs.getString(KEY_LESSONS_PREFIX + groupId, "");
        return json != null && !json.isEmpty();
    }

    public String getLessonsJson(String groupId) {
        return prefs.getString(KEY_LESSONS_PREFIX + groupId, "");
    }

    public void saveLessonsJson(String groupId, String json) {
        prefs.edit().putString(KEY_LESSONS_PREFIX + groupId, json).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
