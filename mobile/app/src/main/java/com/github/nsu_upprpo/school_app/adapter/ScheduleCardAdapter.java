package com.github.nsu_upprpo.school_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.model.ScheduleItem;

import java.util.List;

public class ScheduleCardAdapter extends RecyclerView.Adapter<ScheduleCardAdapter.ScheduleCardViewHolder> {

    public interface OnScheduleClickListener {
        void onScheduleClick(ScheduleItem item);
    }

    private final List<ScheduleItem> items;
    private final OnScheduleClickListener listener;

    public ScheduleCardAdapter(List<ScheduleItem> items) {
        this(items, null);
    }

    public ScheduleCardAdapter(List<ScheduleItem> items, OnScheduleClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_card, parent, false);
        return new ScheduleCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleCardViewHolder holder, int position) {
        ScheduleItem item = items.get(position);

        holder.titleText.setText(item.getTitle());
        holder.subtitleText.setText(item.getSubtitle());
        holder.timeText.setText(item.getTime());

        holder.colorStripe.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.course_orange)
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onScheduleClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ScheduleCardViewHolder extends RecyclerView.ViewHolder {
        View colorStripe;
        TextView titleText;
        TextView subtitleText;
        TextView timeText;

        public ScheduleCardViewHolder(@NonNull View itemView) {
            super(itemView);
            colorStripe = itemView.findViewById(R.id.colorStripe);
            titleText = itemView.findViewById(R.id.scheduleTitleText);
            subtitleText = itemView.findViewById(R.id.scheduleSubtitleText);
            timeText = itemView.findViewById(R.id.scheduleTimeText);
        }
    }
}
