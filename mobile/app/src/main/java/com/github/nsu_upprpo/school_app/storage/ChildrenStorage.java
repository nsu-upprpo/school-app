package com.github.nsu_upprpo.school_app.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.nsu_upprpo.school_app.model.ChildDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildrenStorage {
    private static final String PREFS = "children_storage";
    private static final String KEY_CHILDREN = "children";
    private static final String KEY_CHILD_BRANCHES = "child_branches";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public ChildrenStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveChildren(List<ChildDto> children) {
        prefs.edit()
                .putString(KEY_CHILDREN, gson.toJson(children))
                .apply();
    }

    public List<ChildDto> getChildren() {
        String json = prefs.getString(KEY_CHILDREN, "");

        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<ChildDto>>() {}.getType();
        List<ChildDto> children = gson.fromJson(json, type);

        return children == null ? new ArrayList<>() : children;
    }

    public boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    public void saveChildBranch(String childId, String branchName) {
        Map<String, String> branches = getChildBranches();
        branches.put(childId, branchName);

        prefs.edit()
                .putString(KEY_CHILD_BRANCHES, gson.toJson(branches))
                .apply();
    }

    public Map<String, String> getChildBranches() {
        String json = prefs.getString(KEY_CHILD_BRANCHES, "");

        if (json.isEmpty()) {
            return new HashMap<>();
        }

        Type type = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> branches = gson.fromJson(json, type);

        return branches == null ? new HashMap<>() : branches;
    }

    public boolean hasBranchForChild(String childId) {
        return getChildBranches().containsKey(childId);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}