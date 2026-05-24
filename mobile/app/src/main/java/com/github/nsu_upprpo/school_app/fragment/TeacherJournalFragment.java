package com.github.nsu_upprpo.school_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.AttendanceApi;
import com.github.nsu_upprpo.school_app.api.GroupApi;
import com.github.nsu_upprpo.school_app.api.ProjectApi;
import com.github.nsu_upprpo.school_app.api.ScheduleApi;
import com.github.nsu_upprpo.school_app.model.AttendanceDto;
import com.github.nsu_upprpo.school_app.model.GroupDto;
import com.github.nsu_upprpo.school_app.model.LessonDto;
import com.github.nsu_upprpo.school_app.model.ProjectDto;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherJournalFragment extends Fragment {

    private enum JournalState {
        COURSES,
        PROJECTS,
        LESSONS,
        STUDENTS
    }

    private TextView titleText;
    private TextView subtitleText;
    private LinearLayout journalContent;
    private TextView submitButton;
    private OnBackPressedCallback backCallback;

    private JournalState state = JournalState.COURSES;
    private GroupDto selectedGroup;
    private ProjectDto selectedProject;
    private LessonDto selectedLesson;

    private String authHeader;
    private GroupApi groupApi;
    private ProjectApi projectApi;
    private ScheduleApi scheduleApi;
    private AttendanceApi attendanceApi;

    private List<GroupDto> currentGroups = new ArrayList<>();
    private List<ProjectDto> currentProjects = new ArrayList<>();
    private List<LessonDto> currentLessons = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_teacher_journal, container, false);

        titleText = view.findViewById(R.id.teacherJournalTitle);
        subtitleText = view.findViewById(R.id.teacherJournalSubtitle);
        journalContent = view.findViewById(R.id.teacherJournalContent);
        submitButton = view.findViewById(R.id.teacherJournalSubmitButton);

        submitButton.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Отправка оценок пока не реализована", Toast.LENGTH_SHORT).show()
        );

        setupBackHandling();
        setupApi();

        if (authHeader == null) {
            showEmpty("Токен не найден");
        } else {
            showCourses();
        }

        return view;
    }

    private void setupBackHandling() {
        backCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                navigateBackInsideJournal();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backCallback);
    }

    private void navigateBackInsideJournal() {
        if (state == JournalState.STUDENTS) {
            showLessonsFromCache();
        } else if (state == JournalState.LESSONS) {
            showProjectsFromCache();
        } else if (state == JournalState.PROJECTS) {
            showCoursesFromCache();
        }
    }

    private void updateBackCallback() {
        if (backCallback != null) {
            backCallback.setEnabled(state != JournalState.COURSES);
        }
    }

    private void setupApi() {
        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

        if (token == null || token.isEmpty()) {
            authHeader = null;
            return;
        }

        authHeader = "Bearer " + token;
        groupApi = ApiClient.getClient().create(GroupApi.class);
        projectApi = ApiClient.getClient().create(ProjectApi.class);
        scheduleApi = ApiClient.getClient().create(ScheduleApi.class);
        attendanceApi = ApiClient.getClient().create(AttendanceApi.class);
    }

    private void showCourses() {
        state = JournalState.COURSES;
        selectedGroup = null;
        selectedProject = null;
        selectedLesson = null;
        updateBackCallback();

        titleText.setText("Журнал");
        subtitleText.setText("Выберите курс");
        submitButton.setVisibility(View.GONE);
        showLoading("Загрузка курсов...");

        groupApi.getTeacherGroups(authHeader).enqueue(new Callback<List<GroupDto>>() {
            @Override
            public void onResponse(Call<List<GroupDto>> call, Response<List<GroupDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    currentGroups = response.body();
                    showCoursesFromCache();
                } else {
                    showEmpty("Не удалось загрузить курсы");
                }
            }

            @Override
            public void onFailure(Call<List<GroupDto>> call, Throwable t) {
                if (!isAdded()) return;
                showEmpty("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void showCoursesFromCache() {
        state = JournalState.COURSES;
        selectedGroup = null;
        selectedProject = null;
        selectedLesson = null;
        updateBackCallback();

        titleText.setText("Журнал");
        subtitleText.setText("Выберите курс");
        submitButton.setVisibility(View.GONE);
        renderCourseCards(currentGroups);
    }

    private void showProjects(GroupDto group) {
        state = JournalState.PROJECTS;
        selectedGroup = group;
        selectedProject = null;
        selectedLesson = null;
        updateBackCallback();

        titleText.setText(safe(group.getCourseName(), "Курс"));
        subtitleText.setText("Выберите проект");
        submitButton.setVisibility(View.GONE);
        showLoading("Загрузка проектов...");

        projectApi.getProjectsByGroup(authHeader, group.getGroupId()).enqueue(new Callback<List<ProjectDto>>() {
            @Override
            public void onResponse(Call<List<ProjectDto>> call, Response<List<ProjectDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    currentProjects = response.body();
                    showProjectsFromCache();
                } else {
                    showEmpty("Не удалось загрузить проекты");
                }
            }

            @Override
            public void onFailure(Call<List<ProjectDto>> call, Throwable t) {
                if (!isAdded()) return;
                showEmpty("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void showProjectsFromCache() {
        state = JournalState.PROJECTS;
        selectedProject = null;
        selectedLesson = null;
        updateBackCallback();

        titleText.setText(safe(selectedGroup == null ? null : selectedGroup.getCourseName(), "Курс"));
        subtitleText.setText("Выберите проект");
        submitButton.setVisibility(View.GONE);
        renderProjectCards(currentProjects);
    }

    private void showLessons(ProjectDto project) {
        state = JournalState.LESSONS;
        selectedProject = project;
        selectedLesson = null;
        updateBackCallback();

        titleText.setText(safe(project.getName(), "Проект"));
        subtitleText.setText("Выберите пару");
        submitButton.setVisibility(View.GONE);
        showLoading("Загрузка пар...");

        scheduleApi.getGroupSchedule(authHeader, selectedGroup.getGroupId()).enqueue(new Callback<List<LessonDto>>() {
            @Override
            public void onResponse(Call<List<LessonDto>> call, Response<List<LessonDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    currentLessons = filterLessonsByProject(response.body(), project.getId());
                    showLessonsFromCache();
                } else {
                    showEmpty("Не удалось загрузить пары");
                }
            }

            @Override
            public void onFailure(Call<List<LessonDto>> call, Throwable t) {
                if (!isAdded()) return;
                showEmpty("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void showLessonsFromCache() {
        state = JournalState.LESSONS;
        selectedLesson = null;
        updateBackCallback();

        titleText.setText(safe(selectedProject == null ? null : selectedProject.getName(), "Проект"));
        subtitleText.setText("Выберите пару");
        submitButton.setVisibility(View.GONE);
        renderLessonCards(currentLessons);
    }

    private void showStudents(LessonDto lesson) {
        state = JournalState.STUDENTS;
        selectedLesson = lesson;
        updateBackCallback();

        titleText.setText(safe(lesson.getTopic(), "Пара"));
        subtitleText.setText(formatLessonTime(lesson));
        submitButton.setVisibility(View.VISIBLE);
        showLoading("Загрузка учеников...");

        attendanceApi.getLessonAttendances(authHeader, lesson.getId()).enqueue(new Callback<List<AttendanceDto>>() {
            @Override
            public void onResponse(Call<List<AttendanceDto>> call, Response<List<AttendanceDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    renderStudentCards(response.body());
                } else {
                    showEmpty("Не удалось загрузить учеников");
                }
            }

            @Override
            public void onFailure(Call<List<AttendanceDto>> call, Throwable t) {
                if (!isAdded()) return;
                showEmpty("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private List<LessonDto> filterLessonsByProject(List<LessonDto> lessons, String projectId) {
        List<LessonDto> result = new ArrayList<>();

        for (LessonDto lesson : lessons) {
            if (projectId == null || projectId.equals(lesson.getProjectId())) {
                result.add(lesson);
            }
        }

        return result;
    }

    private void renderCourseCards(List<GroupDto> groups) {
        journalContent.removeAllViews();

        if (groups == null || groups.isEmpty()) {
            showEmpty("У преподавателя пока нет курсов");
            return;
        }

        for (GroupDto group : groups) {
            LinearLayout card = createCard();
            card.addView(createTitle(safe(group.getCourseName(), "Курс")));
            card.addView(createSubtitle(safe(group.getScheduleDescription(), "Расписание не указано")));
            card.addView(createSubtitle("Ученики: " + group.getCurrentStudents() + "/" + group.getMaxStudents()));
            card.addView(createAction("Открыть проекты  ›"));
            card.setOnClickListener(v -> showProjects(group));
            journalContent.addView(card);
        }
    }

    private void renderProjectCards(List<ProjectDto> projects) {
        journalContent.removeAllViews();

        if (projects == null || projects.isEmpty()) {
            showEmpty("Проектов для этого курса пока нет");
            return;
        }

        for (ProjectDto project : projects) {
            LinearLayout card = createCard();
            card.addView(createTitle(safe(project.getName(), "Проект")));
            card.addView(createSubtitle("Пар: " + project.getTotalLessons() + " • максимум " + project.getMaxScore() + " баллов"));
            card.addView(createAction("Открыть пары  ›"));
            card.setOnClickListener(v -> showLessons(project));
            journalContent.addView(card);
        }
    }

    private void renderLessonCards(List<LessonDto> lessons) {
        journalContent.removeAllViews();

        if (lessons == null || lessons.isEmpty()) {
            showEmpty("Пар для этого проекта пока нет");
            return;
        }

        for (LessonDto lesson : lessons) {
            LinearLayout card = createCard();
            card.addView(createTitle(safe(lesson.getTopic(), "Пара")));
            card.addView(createSubtitle(formatLessonTime(lesson)));
            card.addView(createAction("Открыть учеников  ›"));
            card.setOnClickListener(v -> showStudents(lesson));
            journalContent.addView(card);
        }
    }

    private void renderStudentCards(List<AttendanceDto> attendances) {
        journalContent.removeAllViews();

        if (attendances == null || attendances.isEmpty()) {
            showEmpty("Список учеников для этой пары пуст");
            return;
        }

        submitButton.setVisibility(View.VISIBLE);

        for (AttendanceDto attendance : attendances) {
            StudentGradeItem student = new StudentGradeItem(
                    safe(attendance.getChildName(), "Ученик"),
                    0
            );

            LinearLayout card = createCard();
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(android.view.Gravity.CENTER_VERTICAL);

            LinearLayout textBlock = new LinearLayout(requireContext());
            textBlock.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

            TextView nameText = createTitle(student.name);
            TextView gradeLabel = createSubtitle("Оценка");
            textBlock.addView(nameText);
            textBlock.addView(gradeLabel);

            TextView gradeText = createGradeValue(student.grade);
            TextView minusButton = createGradeButton("-");
            TextView plusButton = createGradeButton("+");

            minusButton.setOnClickListener(v -> {
                if (student.grade > 0) {
                    student.grade--;
                    gradeText.setText(String.valueOf(student.grade));
                }
            });

            plusButton.setOnClickListener(v -> {
                student.grade++;
                gradeText.setText(String.valueOf(student.grade));
            });

            card.addView(textBlock, textParams);
            card.addView(gradeText);
            card.addView(minusButton);
            card.addView(plusButton);
            journalContent.addView(card);
        }
    }

    private void showLoading(String message) {
        journalContent.removeAllViews();
        submitButton.setVisibility(View.GONE);
        TextView textView = createSubtitle(message);
        journalContent.addView(textView);
    }

    private void showEmpty(String message) {
        journalContent.removeAllViews();
        TextView textView = createSubtitle(message);
        journalContent.addView(textView);
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setClickable(true);
        card.setFocusable(true);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setElevation(dp(2));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);

        return card;
    }

    private TextView createTitle(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        textView.setTextSize(17);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        return textView;
    }

    private TextView createSubtitle(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint_gray));
        textView.setTextSize(14);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(6), 0, 0);
        textView.setLayoutParams(params);

        return textView;
    }

    private TextView createAction(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange_main));
        textView.setTextSize(15);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(10), 0, 0);
        textView.setLayoutParams(params);

        return textView;
    }

    private TextView createGradeButton(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        textView.setTextSize(18);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        textView.setBackgroundResource(R.drawable.bg_button_orange);
        textView.setClickable(true);
        textView.setFocusable(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(34), dp(34));
        params.setMargins(dp(6), 0, 0, 0);
        textView.setLayoutParams(params);

        return textView;
    }

    private TextView createGradeValue(int grade) {
        TextView textView = new TextView(requireContext());
        textView.setText(String.valueOf(grade));
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        textView.setTextSize(16);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(34), dp(34));
        params.setMargins(dp(8), 0, 0, 0);
        textView.setLayoutParams(params);

        return textView;
    }

    private String formatLessonTime(LessonDto lesson) {
        return formatDate(lesson.getStartTime()) + " • " + formatTime(lesson.getStartTime()) + "–" + formatTime(lesson.getEndTime());
    }

    private String formatDate(String dateTime) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("d MMMM", new Locale("ru"));
            Date date = input.parse(dateTime);
            return date == null ? "" : output.format(date);
        } catch (ParseException | NullPointerException e) {
            return "";
        }
    }

    private String formatTime(String dateTime) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = input.parse(dateTime);
            return date == null ? "" : output.format(date);
        } catch (ParseException | NullPointerException e) {
            return "";
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class StudentGradeItem {
        final String name;
        int grade;

        StudentGradeItem(String name, int grade) {
            this.name = name;
            this.grade = grade;
        }
    }
}
