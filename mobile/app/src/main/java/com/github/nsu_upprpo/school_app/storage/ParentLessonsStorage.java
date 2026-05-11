package com.github.nsu_upprpo.school_app.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.nsu_upprpo.school_app.model.ParentLessonItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ParentLessonsStorage {
    private static final String PREFS = "parent_lessons_storage";

    private static final String KEY_FUTURE_LESSONS = "future_lessons";
    private static final String KEY_MISSED_LESSONS = "missed_lessons";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public ParentLessonsStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveFutureLessons(List<ParentLessonItem> lessons) {
        prefs.edit()
                .putString(KEY_FUTURE_LESSONS, gson.toJson(lessons))
                .apply();
    }

    public void saveMissedLessons(List<ParentLessonItem> lessons) {
        prefs.edit()
                .putString(KEY_MISSED_LESSONS, gson.toJson(lessons))
                .apply();
    }

    public List<ParentLessonItem> getFutureLessons() {
        return getLessonsByKey(KEY_FUTURE_LESSONS);
    }

    public List<ParentLessonItem> getMissedLessons() {
        return getLessonsByKey(KEY_MISSED_LESSONS);
    }

    public boolean hasFutureLessons() {
        return !getFutureLessons().isEmpty();
    }

    public boolean hasAnyLessons() {
        return !getFutureLessons().isEmpty() || !getMissedLessons().isEmpty();
    }

    private List<ParentLessonItem> getLessonsByKey(String key) {
        String json = prefs.getString(key, "");

        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<ParentLessonItem>>() {}.getType();
        List<ParentLessonItem> lessons = gson.fromJson(json, type);

        return lessons == null ? new ArrayList<>() : lessons;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}