package com.github.nsu_upprpo.school_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.model.ScheduleDay;

import java.util.ArrayList;
import java.util.List;

public class ScheduleDayAdapter extends RecyclerView.Adapter<ScheduleDayAdapter.ScheduleDayViewHolder> {

    private final List<ScheduleDay> days = new ArrayList<>();

    public ScheduleDayAdapter(List<ScheduleDay> initialDays) {
        if (initialDays != null) {
            days.addAll(initialDays);
        }
    }

    public void updateDays(List<ScheduleDay> newDays) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScheduleDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_day, parent, false);
        return new ScheduleDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleDayViewHolder holder, int position) {
        ScheduleDay day = days.get(position);

        holder.dayTitleText.setText(day.getDayTitle());

        if (day.getItems() == null || day.getItems().isEmpty()) {
            holder.cardsRecyclerView.setVisibility(View.GONE);
            holder.emptyDayText.setVisibility(View.VISIBLE);
        } else {
            holder.cardsRecyclerView.setVisibility(View.VISIBLE);
            holder.emptyDayText.setVisibility(View.GONE);

            holder.cardsRecyclerView.setLayoutManager(
                    new LinearLayoutManager(holder.itemView.getContext())
            );
            holder.cardsRecyclerView.setAdapter(
                    new ScheduleCardAdapter(day.getItems())
            );
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class ScheduleDayViewHolder extends RecyclerView.ViewHolder {
        TextView dayTitleText;
        TextView emptyDayText;
        RecyclerView cardsRecyclerView;

        public ScheduleDayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTitleText = itemView.findViewById(R.id.dayTitleText);
            emptyDayText = itemView.findViewById(R.id.emptyDayText);
            cardsRecyclerView = itemView.findViewById(R.id.dayCardsRecyclerView);
        }
    }
}