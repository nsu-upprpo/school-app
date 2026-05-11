package com.github.nsu_upprpo.school_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.model.LessonDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeacherLessonAdapter extends RecyclerView.Adapter<TeacherLessonAdapter.LessonViewHolder> {

    public interface OnLessonClickListener {
        void onLessonClick(LessonDto lesson);
    }

    private final List<LessonDto> lessons = new ArrayList<>();
    private final OnLessonClickListener listener;

    public TeacherLessonAdapter(OnLessonClickListener listener) {
        this.listener = listener;
    }

    public void updateLessons(List<LessonDto> newLessons) {
        lessons.clear();

        if (newLessons != null) {
            lessons.addAll(newLessons);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        LessonDto lesson = lessons.get(position);

        holder.dateText.setText(
                formatDate(lesson.getStartTime()) + " • " +
                        formatTime(lesson.getStartTime()) + "–" +
                        formatTime(lesson.getEndTime())
        );

        holder.topicText.setText("Тема: " + safe(lesson.getTopic()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLessonClick(lesson);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    private String formatDate(String dateTime) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("d MMMM", new Locale("ru"));
            return output.format(input.parse(dateTime));
        } catch (ParseException | NullPointerException e) {
            return safe(dateTime);
        }
    }

    private String formatTime(String dateTime) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return output.format(input.parse(dateTime));
        } catch (ParseException | NullPointerException e) {
            return "";
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        TextView topicText;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);

            dateText = itemView.findViewById(R.id.lessonDateText);
            topicText = itemView.findViewById(R.id.lessonTopicText);
        }
    }
}