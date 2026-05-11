package com.github.nsu_upprpo.school_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.model.ParentLessonItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

public class ParentLessonAdapter extends RecyclerView.Adapter<ParentLessonAdapter.ParentLessonViewHolder> {

    private final List<ParentLessonItem> lessons = new ArrayList<>();

    public interface OnLessonClickListener {
        void onLessonClick(ParentLessonItem lesson);
    }

    private OnLessonClickListener listener;

    public void setOnLessonClickListener(OnLessonClickListener listener) {
        this.listener = listener;
    }

    public void updateLessons(List<ParentLessonItem> newLessons) {
        lessons.clear();

        if (newLessons != null) {
            lessons.addAll(newLessons);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ParentLessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parent_lesson, parent, false);
        return new ParentLessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentLessonViewHolder holder, int position) {
        ParentLessonItem lesson = lessons.get(position);

        holder.lessonDateText.setText(formatDate(lesson.getDateMillis()));
        holder.lessonCourseText.setText(makeCourseText(lesson.getCourseName()));
        holder.lessonTeacherText.setText("Преподаватель: " + lesson.getTeacherName());
        holder.lessonTimeText.setText(lesson.getStartTime() + "-" + lesson.getEndTime());

        holder.lessonTopicText.setText("Тема: " + lesson.getTopic());

        if ("ABSENT".equalsIgnoreCase(lesson.getStatus())) {
            holder.lessonStatusText.setVisibility(View.VISIBLE);
            holder.lessonStatusText.setText("Пропущено");
        } else {
            holder.lessonStatusText.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLessonClick(lesson);
            }
        });
    }

    private SpannableString makeCourseText(String courseName) {
        String label = "Курс: ";
        String text = label + courseName;

        SpannableString spannable = new SpannableString(text);

        spannable.setSpan(
                new StyleSpan(Typeface.BOLD),
                label.length(),
                text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        return spannable;
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    private String formatDate(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("d MMMM, EEEE", new Locale("ru"));
        return format.format(millis);
    }

    static class ParentLessonViewHolder extends RecyclerView.ViewHolder {
        TextView lessonDateText;
        TextView lessonCourseText;
        TextView lessonTeacherText;
        TextView lessonTimeText;
        TextView lessonTopicText;
        TextView lessonStatusText;

        public ParentLessonViewHolder(@NonNull View itemView) {
            super(itemView);

            lessonDateText = itemView.findViewById(R.id.lessonDateText);
            lessonCourseText = itemView.findViewById(R.id.lessonCourseText);
            lessonTeacherText = itemView.findViewById(R.id.lessonTeacherText);
            lessonTimeText = itemView.findViewById(R.id.lessonTimeText);
            lessonTopicText = itemView.findViewById(R.id.lessonTopicText);
            lessonStatusText = itemView.findViewById(R.id.lessonStatusText);
        }
    }
}