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
    private static final String KEY_LESSONS = "lessons";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public ParentLessonsStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveLessons(List<ParentLessonItem> lessons) {
        prefs.edit()
                .putString(KEY_LESSONS, gson.toJson(lessons))
                .apply();
    }

    public List<ParentLessonItem> getLessons() {
        String json = prefs.getString(KEY_LESSONS, "");

        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<ParentLessonItem>>() {}.getType();
        List<ParentLessonItem> lessons = gson.fromJson(json, type);

        return lessons == null ? new ArrayList<>() : lessons;
    }

    public boolean hasLessons() {
        return !getLessons().isEmpty();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}