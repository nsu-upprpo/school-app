package com.github.nsu_upprpo.school_app.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.ParentLessonActionApi;
import com.github.nsu_upprpo.school_app.model.ParentLessonItem;
import com.github.nsu_upprpo.school_app.model.RescheduleLessonRequest;
import com.github.nsu_upprpo.school_app.storage.ParentLessonsStorage;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentLessonRescheduleFragment extends Fragment {
    public static final String RESULT_KEY = "parent_lesson_reschedule_result";
    public static final String RESULT_REFRESH = "refresh_parent_lessons";
    private static final String TAG = "ParentLessons";

    private static final String ARG_SOURCE_LESSON = "source_lesson";
    private static final String ARG_TARGET_LESSONS = "target_lessons";
    private static final String ARG_SOURCE_LESSON_ID = "source_lesson_id";
    private static final String ARG_SOURCE_CHILD_ID = "source_child_id";
    private static final String ARG_SOURCE_COURSE_NAME = "source_course_name";
    private static final String ARG_SOURCE_TOPIC = "source_topic";
    private static final String ARG_SOURCE_TEACHER_NAME = "source_teacher_name";
    private static final String ARG_SOURCE_CHILD_NAME = "source_child_name";
    private static final String ARG_SOURCE_CHILD_STATUS = "source_child_status";
    private static final String ARG_SOURCE_START_TIME = "source_start_time";
    private static final String ARG_SOURCE_END_TIME = "source_end_time";
    private static final String ARG_SOURCE_DATE_MILLIS = "source_date_millis";

    private TextView sourceDateText;
    private TextView sourceTimeText;
    private TextView sourceCourseText;
    private TextView sourceTopicText;
    private TextView sourceTeacherText;
    private TextView sourceChildText;
    private TextView sourceStatusText;
    private LinearLayout sourceBadgesRow;
    private LinearLayout targetLessonsLayout;
    private TextView emptyTargetsText;
    private Button confirmButton;
    private ImageView backButton;

    private String sourceLessonId = "";
    private String sourceChildId = "";
    private String sourceCourseName = "";
    private String sourceTopic = "";
    private String sourceTeacherName = "";
    private String sourceChildName = "";
    private String sourceChildStatus = "";
    private String sourceStartTime = "";
    private String sourceEndTime = "";
    private long sourceDateMillis;
    private ArrayList<ParentLessonItem> targetLessons = new ArrayList<>();
    private ParentLessonItem selectedTargetLesson;
    private View selectedTargetView;

    public static ParentLessonRescheduleFragment newInstance(ParentLessonItem sourceLesson,
                                                             ArrayList<ParentLessonItem> targetLessons) {
        ParentLessonRescheduleFragment fragment = new ParentLessonRescheduleFragment();
        Bundle args = new Bundle();
        if (sourceLesson != null) {
            args.putString(ARG_SOURCE_LESSON_ID, sourceLesson.getLessonId());
            args.putString(ARG_SOURCE_CHILD_ID, sourceLesson.getChildId());
            args.putString(ARG_SOURCE_COURSE_NAME, sourceLesson.getCourseName());
            args.putString(ARG_SOURCE_TOPIC, sourceLesson.getTopic());
            args.putString(ARG_SOURCE_TEACHER_NAME, sourceLesson.getTeacherName());
            args.putString(ARG_SOURCE_CHILD_NAME, sourceLesson.getChildNames());
            args.putString(ARG_SOURCE_CHILD_STATUS, sourceLesson.getChildStatus());
            args.putString(ARG_SOURCE_START_TIME, sourceLesson.getStartTime());
            args.putString(ARG_SOURCE_END_TIME, sourceLesson.getEndTime());
            args.putLong(ARG_SOURCE_DATE_MILLIS, sourceLesson.getDateMillis());
        }
        args.putSerializable(ARG_TARGET_LESSONS, targetLessons);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_lesson_reschedule, container, false);

        sourceDateText = view.findViewById(R.id.lessonDateText);
        sourceTimeText = view.findViewById(R.id.lessonTimeText);
        sourceCourseText = view.findViewById(R.id.lessonCourseText);
        sourceTopicText = view.findViewById(R.id.lessonTopicText);
        sourceTeacherText = view.findViewById(R.id.lessonTeacherText);
        sourceChildText = view.findViewById(R.id.lessonChildText);
        sourceStatusText = view.findViewById(R.id.lessonStatusText);
        sourceBadgesRow = view.findViewById(R.id.lessonBadgesRow);
        targetLessonsLayout = view.findViewById(R.id.targetLessonsLayout);
        emptyTargetsText = view.findViewById(R.id.emptyTargetsText);
        confirmButton = view.findViewById(R.id.confirmRescheduleButton);
        backButton = view.findViewById(R.id.rescheduleBackButton);

        readArguments();
        applyConfirmButtonStyle();
        renderSourceLesson();
        renderTargetLessons();

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        confirmButton.setOnClickListener(v -> confirmReschedule());

        return view;
    }

    @SuppressWarnings("unchecked")
    private void readArguments() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        sourceLessonId = safeRaw(args.getString(ARG_SOURCE_LESSON_ID));
        sourceChildId = safeRaw(args.getString(ARG_SOURCE_CHILD_ID));
        sourceCourseName = safeRaw(args.getString(ARG_SOURCE_COURSE_NAME));
        sourceTopic = safeRaw(args.getString(ARG_SOURCE_TOPIC));
        sourceTeacherName = safeRaw(args.getString(ARG_SOURCE_TEACHER_NAME));
        sourceChildName = safeRaw(args.getString(ARG_SOURCE_CHILD_NAME));
        sourceChildStatus = safeRaw(args.getString(ARG_SOURCE_CHILD_STATUS));
        sourceStartTime = safeRaw(args.getString(ARG_SOURCE_START_TIME));
        sourceEndTime = safeRaw(args.getString(ARG_SOURCE_END_TIME));
        sourceDateMillis = args.getLong(ARG_SOURCE_DATE_MILLIS, 0);

        Object targets = args.getSerializable(ARG_TARGET_LESSONS);
        if (targets instanceof ArrayList) {
            targetLessons = (ArrayList<ParentLessonItem>) targets;
        }
    }

    private void renderSourceLesson() {
        if (sourceChildId.isEmpty() || sourceLessonId.isEmpty()) {
            confirmButton.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Не удалось открыть перенос занятия", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }

        sourceDateText.setText(formatDate(sourceDateMillis));
        sourceTimeText.setText(safe(sourceStartTime) + "-" + safe(sourceEndTime));
        sourceCourseText.setText("Курс: " + safe(sourceCourseName));
        sourceTopicText.setText(safe(sourceTopic));
        if (hasRealValue(sourceTeacherName)) {
            sourceTeacherText.setVisibility(View.VISIBLE);
            sourceTeacherText.setText("Преподаватель: " + sourceTeacherName);
        } else {
            sourceTeacherText.setVisibility(View.GONE);
        }
        sourceStatusText.setVisibility(View.GONE);

        if (!sourceChildName.isEmpty() && !"не указано".equalsIgnoreCase(sourceChildName)) {
            sourceChildText.setVisibility(View.VISIBLE);
            sourceChildText.setText(sourceChildName);
            sourceChildText.setBackground(createBadgeBackground(getChildBadgeColor(sourceChildId)));
        } else {
            sourceChildText.setVisibility(View.GONE);
        }

        boolean hasStatusBadge = renderSourceStatusBadge();
        sourceBadgesRow.setVisibility(
                hasStatusBadge || sourceChildText.getVisibility() == View.VISIBLE
                        ? View.VISIBLE
                        : View.GONE
        );
        updateStatusBadgeMargin(sourceStatusText, sourceChildText.getVisibility() == View.VISIBLE);
    }

    private boolean renderSourceStatusBadge() {
        if ("ABSENT".equalsIgnoreCase(sourceChildStatus)) {
            sourceStatusText.setVisibility(View.VISIBLE);
            sourceStatusText.setText("Пропуск");
            sourceStatusText.setBackground(createBadgeBackground(Color.parseColor("#6CBF4A")));
            return true;
        }

        if ("CANCELLED_BY_PARENT".equalsIgnoreCase(sourceChildStatus)) {
            sourceStatusText.setVisibility(View.VISIBLE);
            sourceStatusText.setText("Отменено");
            sourceStatusText.setBackground(createBadgeBackground(Color.parseColor("#F05A5A")));
            return true;
        }

        if ("RESCHEDULED_OUT".equalsIgnoreCase(sourceChildStatus)) {
            sourceStatusText.setVisibility(View.VISIBLE);
            sourceStatusText.setText("Перенесено");
            sourceStatusText.setBackground(createBadgeBackground(Color.parseColor("#FF6B00")));
            return true;
        }

        if ("RESCHEDULED_IN".equalsIgnoreCase(sourceChildStatus)) {
            sourceStatusText.setVisibility(View.VISIBLE);
            sourceStatusText.setText("Перенесено");
            sourceStatusText.setBackground(createBadgeBackground(Color.parseColor("#A56BE8")));
            return true;
        }

        sourceStatusText.setVisibility(View.GONE);
        return false;
    }

    private void renderTargetLessons() {
        targetLessonsLayout.removeAllViews();

        if (targetLessons == null || targetLessons.isEmpty()) {
            emptyTargetsText.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.GONE);
            return;
        }

        emptyTargetsText.setVisibility(View.GONE);
        confirmButton.setVisibility(View.VISIBLE);
        confirmButton.setEnabled(false);

        for (ParentLessonItem lesson : targetLessons) {
            targetLessonsLayout.addView(createTargetLessonView(lesson));
        }
    }

    private View createTargetLessonView(ParentLessonItem lesson) {
        LinearLayout card = new LinearLayout(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(params);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.bg_card);

        TextView titleText = new TextView(requireContext());
        titleText.setText(safe(lesson.getTopic()));
        titleText.setTextColor(getResources().getColor(R.color.text_dark));
        titleText.setTextSize(16);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(titleText);

        TextView detailsText = new TextView(requireContext());
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        detailsParams.setMargins(0, dp(6), 0, 0);
        detailsText.setLayoutParams(detailsParams);
        detailsText.setText(formatDate(lesson.getDateMillis()) + "  " +
                safe(lesson.getStartTime()) + "-" + safe(lesson.getEndTime()));
        detailsText.setTextColor(getResources().getColor(R.color.hint_gray));
        detailsText.setTextSize(14);
        card.addView(detailsText);

        card.setOnClickListener(v -> selectTargetLesson(lesson, card));
        return card;
    }

    private void selectTargetLesson(ParentLessonItem lesson, View targetView) {
        if (selectedTargetView != null) {
            selectedTargetView.setBackgroundResource(R.drawable.bg_card);
        }

        selectedTargetLesson = lesson;
        selectedTargetView = targetView;
        selectedTargetView.setBackgroundResource(R.drawable.bg_payment_unpaid);
        confirmButton.setEnabled(true);
    }

    private void confirmReschedule() {
        if (sourceLessonId.isEmpty() || sourceChildId.isEmpty() || selectedTargetLesson == null) {
            Toast.makeText(requireContext(), "Выберите занятие для переноса", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = getCurrentAuthHeader();
        if (authHeader.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        confirmButton.setEnabled(false);

        ParentLessonActionApi actionApi = ApiClient.getClient().create(ParentLessonActionApi.class);
        RescheduleLessonRequest request = new RescheduleLessonRequest(
                selectedTargetLesson.getLessonId(),
                "Перенос занятия родителем"
        );

        actionApi.rescheduleLesson(
                authHeader,
                sourceChildId,
                sourceLessonId,
                request
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful()) {
                    new ParentLessonsStorage(requireContext()).clear();
                    showSuccessDialog();
                } else {
                    String errorBody = getErrorBodyString(response);
                    logRescheduleFailure(response.code(), errorBody);
                    confirmButton.setEnabled(true);
                    Toast.makeText(requireContext(), getRescheduleErrorMessage(response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }

                confirmButton.setEnabled(true);
                Log.e(TAG, "reschedule request failed: " + t.getMessage());
                Toast.makeText(requireContext(), "Не удалось перенести занятие", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getRescheduleErrorMessage(int code) {
        if (code == 400) {
            return "Нельзя перенести занятие на выбранную дату";
        }
        if (code == 409) {
            return "Не удалось перенести занятие. Возможно, оно уже перенесено или выбранная дата недоступна.";
        }
        if (code == 404) {
            return "Занятие не найдено";
        }

        return "Не удалось перенести занятие";
    }

    private void logRescheduleFailure(int code, String errorBody) {
        Log.e(TAG, "reschedule failed code=" + code
                + ", body=" + errorBody);
    }

    private void applyConfirmButtonStyle() {
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_enabled},
                new int[]{}
        };
        int[] colors = new int[]{
                0xFFCFCFCF,
                ContextCompat.getColor(requireContext(), R.color.orange_button)
        };

        confirmButton.setBackgroundTintList(new ColorStateList(states, colors));
        confirmButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
    }

    private void showSuccessDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_reschedule_success, null, false);

        TextView messageText = dialogView.findViewById(R.id.rescheduleSuccessMessage);
        Button actionButton = dialogView.findViewById(R.id.rescheduleSuccessButton);
        actionButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.orange_button)
        ));
        actionButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        messageText.setText("Перенесли занятие с "
                + formatShortDate(sourceDateMillis)
                + " на "
                + formatShortDate(selectedTargetLesson.getDateMillis()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        actionButton.setOnClickListener(v -> {
            dialog.dismiss();
            Bundle result = new Bundle();
            result.putBoolean(RESULT_REFRESH, true);
            getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
            getParentFragmentManager().popBackStack();
        });

        dialog.show();
    }

    private String getErrorBodyString(Response<?> response) {
        if (response.errorBody() == null) {
            return "";
        }

        try {
            return response.errorBody().string();
        } catch (IOException e) {
            return "";
        }
    }

    private String getCurrentAuthHeader() {
        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            return "";
        }

        return "Bearer " + token;
    }

    private String formatDate(long millis) {
        return new SimpleDateFormat("d MMMM (EEEE)", new Locale("ru")).format(new Date(millis));
    }

    private String formatShortDate(long millis) {
        return new SimpleDateFormat("d MMMM", new Locale("ru")).format(new Date(millis));
    }

    private String safe(String value) {
        return value == null || value.isEmpty() ? "не указано" : value;
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

    private void updateStatusBadgeMargin(TextView statusText, boolean hasChildBadge) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) statusText.getLayoutParams();
        params.setMarginStart(hasChildBadge ? dp(8) : 0);
        statusText.setLayoutParams(params);
    }

    private String safeRaw(String value) {
        return value == null ? "" : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
