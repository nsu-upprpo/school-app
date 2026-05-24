package com.github.nsu_upprpo.school_app.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.nsu_upprpo.school_app.model.BonusJournalDto;
import com.github.nsu_upprpo.school_app.model.ChildDto;
import com.github.nsu_upprpo.school_app.model.GroupDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ParentChildProfileStorage {
    private static final String PREFS = "parent_child_profile_storage";
    private static final String KEY_CHILD_PREFIX = "child_";
    private static final String KEY_BONUS_JOURNAL_PREFIX = "bonus_journal_";
    private static final String KEY_PARENT_GROUPS = "parent_groups";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public ParentChildProfileStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveChild(String childId, ChildDto child) {
        if (childId == null || childId.isEmpty() || child == null) {
            return;
        }

        prefs.edit()
                .putString(KEY_CHILD_PREFIX + childId, gson.toJson(child))
                .apply();
    }

    public ChildDto getChild(String childId) {
        if (childId == null || childId.isEmpty()) {
            return null;
        }

        String json = prefs.getString(KEY_CHILD_PREFIX + childId, "");

        if (json.isEmpty()) {
            return null;
        }

        return gson.fromJson(json, ChildDto.class);
    }

    public void saveParentGroups(List<GroupDto> groups) {
        prefs.edit()
                .putString(KEY_PARENT_GROUPS, gson.toJson(groups == null ? new ArrayList<>() : groups))
                .apply();
    }

    public List<GroupDto> getParentGroups() {
        String json = prefs.getString(KEY_PARENT_GROUPS, "");

        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<GroupDto>>() {}.getType();
        List<GroupDto> groups = gson.fromJson(json, type);

        return groups == null ? new ArrayList<>() : groups;
    }

    public void saveBonusJournal(String childId, List<BonusJournalDto> journal) {
        if (childId == null || childId.isEmpty()) {
            return;
        }

        prefs.edit()
                .putString(KEY_BONUS_JOURNAL_PREFIX + childId, gson.toJson(journal == null ? new ArrayList<>() : journal))
                .apply();
    }

    public List<BonusJournalDto> getBonusJournal(String childId) {
        if (childId == null || childId.isEmpty()) {
            return new ArrayList<>();
        }

        String json = prefs.getString(KEY_BONUS_JOURNAL_PREFIX + childId, "");

        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<BonusJournalDto>>() {}.getType();
        List<BonusJournalDto> journal = gson.fromJson(json, type);

        return journal == null ? new ArrayList<>() : journal;
    }

    public boolean hasChildProfile(String childId) {
        return getChild(childId) != null || prefs.contains(KEY_BONUS_JOURNAL_PREFIX + childId);
    }

    public boolean hasBonusJournal(String childId) {
        return childId != null && !childId.isEmpty() && prefs.contains(KEY_BONUS_JOURNAL_PREFIX + childId);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
