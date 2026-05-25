package com.github.nsu_upprpo.school_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class ParentLessonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LESSON = 0;
    private static final int TYPE_FOOTER = 1;

    private final List<ParentLessonItem> lessons = new ArrayList<>();
    private boolean showChildName;
    private boolean showFooter;
    private boolean showAll;

    public interface OnLessonClickListener {
        void onLessonClick(ParentLessonItem lesson);
    }

    private OnLessonClickListener listener;
    private View.OnClickListener footerClickListener;

    public void setOnLessonClickListener(OnLessonClickListener listener) {
        this.listener = listener;
    }

    public void updateLessons(List<ParentLessonItem> newLessons) {
        updateLessons(newLessons, false, false, null);
    }

    public void updateLessons(List<ParentLessonItem> newLessons,
                              boolean showFooter,
                              boolean showAll,
                              View.OnClickListener footerClickListener) {
        lessons.clear();

        if (newLessons != null) {
            lessons.addAll(newLessons);
        }

        this.showFooter = showFooter;
        this.showAll = showAll;
        this.footerClickListener = footerClickListener;
        notifyDataSetChanged();
    }

    public void setShowChildName(boolean showChildName) {
        this.showChildName = showChildName;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_show_all_lessons, parent, false);
            return new FooterViewHolder(view);
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parent_lesson, parent, false);
        return new ParentLessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == TYPE_FOOTER) {
            FooterViewHolder holder = (FooterViewHolder) viewHolder;
            holder.footerText.setText(showAll ? "Скрыть" : "Показать все");
            holder.itemView.setOnClickListener(footerClickListener);
            return;
        }

        ParentLessonViewHolder holder = (ParentLessonViewHolder) viewHolder;
        ParentLessonItem lesson = lessons.get(position);

        holder.lessonDateText.setText(formatDate(lesson.getDateMillis()));
        holder.lessonCourseText.setText(makeCourseText(lesson.getCourseName()));
        holder.lessonTimeText.setText(lesson.getStartTime() + "-" + lesson.getEndTime());
        holder.lessonTopicText.setText(lesson.getTopic());

        if (hasRealValue(lesson.getTeacherName())) {
            holder.lessonTeacherText.setVisibility(View.VISIBLE);
            holder.lessonTeacherText.setText("Преподаватель: " + lesson.getTeacherName());
        } else {
            holder.lessonTeacherText.setVisibility(View.GONE);
        }

        if (showChildName && lesson.getChildNames() != null && !lesson.getChildNames().isEmpty()
                && !"не указано".equalsIgnoreCase(lesson.getChildNames())) {
            holder.lessonChildText.setVisibility(View.VISIBLE);
            holder.lessonChildText.setText(lesson.getChildNames());
            holder.lessonChildText.setBackground(createBadgeBackground(getChildBadgeColor(lesson.getChildId())));
        } else {
            holder.lessonChildText.setVisibility(View.GONE);
        }

        boolean hasStatusBadge = true;
        if ("ABSENT".equalsIgnoreCase(lesson.getStatus())
                || "ABSENT".equalsIgnoreCase(lesson.getChildStatus())) {
            holder.lessonStatusText.setVisibility(View.VISIBLE);
            holder.lessonStatusText.setText("Пропуск");
            holder.lessonStatusText.setBackground(createBadgeBackground(Color.parseColor("#6CBF4A")));
        } else if ("CANCELLED_BY_PARENT".equalsIgnoreCase(lesson.getChildStatus())
                || "CANCELLED_BY_PARENT".equalsIgnoreCase(lesson.getStatus())) {
            holder.lessonStatusText.setVisibility(View.VISIBLE);
            holder.lessonStatusText.setText("Отменено");
            holder.lessonStatusText.setBackground(createBadgeBackground(Color.parseColor("#F05A5A")));
        } else if ("RESCHEDULED_OUT".equalsIgnoreCase(lesson.getChildStatus())) {
            holder.lessonStatusText.setVisibility(View.VISIBLE);
            holder.lessonStatusText.setText("Перенесено");
            holder.lessonStatusText.setBackground(createBadgeBackground(Color.parseColor("#FF6B00")));
        } else if ("RESCHEDULED_IN".equalsIgnoreCase(lesson.getChildStatus())) {
            holder.lessonStatusText.setVisibility(View.VISIBLE);
            holder.lessonStatusText.setText("Перенесено сюда");
            holder.lessonStatusText.setBackground(createBadgeBackground(Color.parseColor("#A56BE8")));
        } else {
            holder.lessonStatusText.setVisibility(View.GONE);
            hasStatusBadge = false;
        }

        holder.lessonBadgesRow.setVisibility(
                hasStatusBadge || holder.lessonChildText.getVisibility() == View.VISIBLE
                        ? View.VISIBLE
                        : View.GONE
        );
        updateChildBadgeMargin(holder.lessonChildText, hasStatusBadge);

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

    private boolean hasRealValue(String value) {
        return value != null && !value.isEmpty() && !"не указано".equalsIgnoreCase(value);
    }

    private GradientDrawable createBadgeBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(32f);
        return drawable;
    }

    private int getChildBadgeColor(String childId) {
        int[] colors = {
                Color.parseColor("#F6E0A8"),
                Color.parseColor("#E8D6FF"),
                Color.parseColor("#DDE8FF"),
                Color.parseColor("#DDF5E3"),
                Color.parseColor("#FFE0D4")
        };

        if (childId == null || childId.isEmpty()) {
            return colors[0];
        }

        return colors[Math.abs(childId.hashCode()) % colors.length];
    }

    private void updateChildBadgeMargin(TextView childText, boolean hasStatusBadge) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) childText.getLayoutParams();
        params.setMarginStart(hasStatusBadge ? dp(childText, 8) : 0);
        childText.setLayoutParams(params);
    }

    private int dp(View view, int value) {
        return (int) (value * view.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public int getItemCount() {
        return lessons.size() + (showFooter ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return showFooter && position == lessons.size() ? TYPE_FOOTER : TYPE_LESSON;
    }

    private String formatDate(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("d MMMM (EEEE)", new Locale("ru"));
        return format.format(millis);
    }

    static class ParentLessonViewHolder extends RecyclerView.ViewHolder {
        TextView lessonDateText;
        TextView lessonCourseText;
        TextView lessonTeacherText;
        TextView lessonTimeText;
        TextView lessonTopicText;
        TextView lessonStatusText;
        TextView lessonChildText;
        LinearLayout lessonBadgesRow;

        public ParentLessonViewHolder(@NonNull View itemView) {
            super(itemView);

            lessonDateText = itemView.findViewById(R.id.lessonDateText);
            lessonCourseText = itemView.findViewById(R.id.lessonCourseText);
            lessonTeacherText = itemView.findViewById(R.id.lessonTeacherText);
            lessonTimeText = itemView.findViewById(R.id.lessonTimeText);
            lessonTopicText = itemView.findViewById(R.id.lessonTopicText);
            lessonStatusText = itemView.findViewById(R.id.lessonStatusText);
            lessonChildText = itemView.findViewById(R.id.lessonChildText);
            lessonBadgesRow = itemView.findViewById(R.id.lessonBadgesRow);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView footerText;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            footerText = itemView.findViewById(R.id.showAllLessonsFooterText);
        }
    }
}
