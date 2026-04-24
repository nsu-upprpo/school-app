package com.github.nsu_upprpo.school_app.storage;

import android.content.Context;
import android.content.SharedPreferences;
public class UserStorage {

    private static final String PREFS = "user_prefs";

    private static final String KEY_PARENT_NAME = "parent_name";
    private static final String KEY_PARENT_PHONE = "parent_phone";
    private static final String KEY_CHILD_NAME = "child_name";
    private static final String KEY_BRANCH = "branch";

    private static final String KEY_TEACHER_NAME = "teacher_name";
    private static final String KEY_TEACHER_PHONE = "teacher_phone";

    private final SharedPreferences prefs;

    public UserStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveParentProfile(String parentName, String parentPhone, String childName, String branch) {
        prefs.edit()
                .putString(KEY_PARENT_NAME, parentName)
                .putString(KEY_PARENT_PHONE, parentPhone)
                .putString(KEY_CHILD_NAME, childName)
                .putString(KEY_BRANCH, branch)
                .apply();
    }

    public String getParentName() {
        return prefs.getString(KEY_PARENT_NAME, "");
    }

    public String getParentPhone() {
        return prefs.getString(KEY_PARENT_PHONE, "");
    }

    public String getChildName() {
        return prefs.getString(KEY_CHILD_NAME, "");
    }

    public String getBranch() {
        return prefs.getString(KEY_BRANCH, "");
    }

    public boolean hasParentProfile() {
        return !getParentName().isEmpty() || !getParentPhone().isEmpty()
                || !getChildName().isEmpty() || !getBranch().isEmpty();
    }

    public void saveTeacherProfile(String teacherName, String teacherPhone) {
        prefs.edit()
                .putString(KEY_TEACHER_NAME, teacherName)
                .putString(KEY_TEACHER_PHONE, teacherPhone)
                .apply();
    }

    public String getTeacherName() {
        return prefs.getString(KEY_TEACHER_NAME, "");
    }

    public String getTeacherPhone() {
        return prefs.getString(KEY_TEACHER_PHONE, "");
    }

    public boolean hasTeacherProfile() {
        return !getTeacherName().isEmpty() || !getTeacherPhone().isEmpty();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
