package com.github.nsu_upprpo.school_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.model.ChildDto;
import com.github.nsu_upprpo.school_app.model.GroupDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ParentChildAdapter extends RecyclerView.Adapter<ParentChildAdapter.ChildViewHolder> {

    private final List<ChildDto> children = new ArrayList<>();
    private final Map<String, String> childBranchNames = new HashMap<>();

    public interface OnChildClickListener {
        void onChildClick(ChildDto child, String branchName);
    }

    private OnChildClickListener listener;

    public void setOnChildClickListener(OnChildClickListener listener) {
        this.listener = listener;
    }

    public void updateChildren(List<ChildDto> newChildren) {
        children.clear();

        if (newChildren != null) {
            children.addAll(newChildren);
        }

        notifyDataSetChanged();
    }

    public void setChildBranchName(String childId, String branchName) {
        if (childId == null || branchName == null || branchName.isEmpty()) {
            return;
        }

        childBranchNames.put(childId, branchName);
        notifyDataSetChanged();
    }

    public void setChildBranchNames(Map<String, String> branchNames) {
        childBranchNames.clear();

        if (branchNames != null) {
            childBranchNames.putAll(branchNames);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parent_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        ChildDto child = children.get(position);

        String fullName = child.getFullName();
        if (fullName == null || fullName.isEmpty()) {
            fullName = "Ребёнок";
        }

        holder.childNameText.setText(fullName);
        holder.childAvatarText.setText(fullName.substring(0, 1).toUpperCase());

        String courseName = getCourseNames(child);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                String branchName = childBranchNames.get(child.getId());
                if (branchName == null || branchName.isEmpty()) {
                    branchName = "не указан";
                }

                listener.onChildClick(child, branchName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    private String getCourseNames(ChildDto child) {
        if (child.getGroups() == null || child.getGroups().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (GroupDto group : child.getGroups()) {
            if (group.getCourseName() == null || group.getCourseName().isEmpty()) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append(", ");
            }

            sb.append(group.getCourseName());
        }

        return sb.toString();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView childAvatarText;
        TextView childNameText;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);

            childAvatarText = itemView.findViewById(R.id.childAvatarText);
            childNameText = itemView.findViewById(R.id.childNameText);
        }
    }
}