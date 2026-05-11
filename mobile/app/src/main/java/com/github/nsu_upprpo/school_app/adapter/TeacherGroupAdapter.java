package com.github.nsu_upprpo.school_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.model.GroupDto;

import java.util.ArrayList;
import java.util.List;

public class TeacherGroupAdapter extends RecyclerView.Adapter<TeacherGroupAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(GroupDto group);
    }

    private final List<GroupDto> groups = new ArrayList<>();
    private final OnGroupClickListener listener;

    public TeacherGroupAdapter(OnGroupClickListener listener) {
        this.listener = listener;
    }

    public void updateGroups(List<GroupDto> newGroups) {
        groups.clear();

        if (newGroups != null) {
            groups.addAll(newGroups);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupDto group = groups.get(position);

        holder.courseText.setText(safe(group.getCourseName()));
        holder.branchText.setText("Филиал: " + safe(group.getBranchName()));
        holder.scheduleText.setText("Расписание: " + safe(group.getScheduleDescription()));
        holder.studentsText.setText("Ученики: " + group.getCurrentStudents() + " / " + group.getMaxStudents());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView courseText;
        TextView branchText;
        TextView scheduleText;
        TextView studentsText;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            courseText = itemView.findViewById(R.id.groupCourseText);
            branchText = itemView.findViewById(R.id.groupBranchText);
            scheduleText = itemView.findViewById(R.id.groupScheduleText);
            studentsText = itemView.findViewById(R.id.groupStudentsText);
        }
    }
}