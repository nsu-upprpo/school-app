package com.github.nsu_upprpo.school_app.adapter;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.model.ChildDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private final List<ChildDto> children = new ArrayList<>();
    private final Map<String, String> childBranchNames = new HashMap<>();

    public void updateChildren(List<ChildDto> newChildren) {
        children.clear();
        childBranchNames.clear();

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

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child_profile, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        ChildDto child = children.get(position);

        holder.childNameText.setText(child.getFullName());

        String birthDate = child.getBirthDate();
        if (birthDate == null || birthDate.isEmpty()) {
            holder.childBirthDateText.setText(makeLabelValueText("Дата рождения: ", "не указана"));
        } else {
            holder.childBirthDateText.setText(makeLabelValueText("Дата рождения: ", birthDate));
        }

        String branchName = childBranchNames.get(child.getId());
        if (branchName == null || branchName.isEmpty()) {
            holder.childBranchText.setText(makeLabelValueText("Филиал: ", "не указан"));
        } else {
            holder.childBranchText.setText(makeLabelValueText("Филиал: ", branchName));
        }
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    private SpannableString makeLabelValueText(String label, String value) {
        String text = label + value;
        SpannableString spannable = new SpannableString(text);

        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#8A8A8A")),
                0,
                label.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#222222")),
                label.length(),
                text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        return spannable;
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView childNameText;
        TextView childBirthDateText;
        TextView childBranchText;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);

            childNameText = itemView.findViewById(R.id.childNameText);
            childBirthDateText = itemView.findViewById(R.id.childBirthDateText);
            childBranchText = itemView.findViewById(R.id.childBranchText);
        }
    }

    public void setChildBranchNames(Map<String, String> branchNames) {
        childBranchNames.clear();

        if (branchNames != null) {
            childBranchNames.putAll(branchNames);
        }

        notifyDataSetChanged();
    }
}