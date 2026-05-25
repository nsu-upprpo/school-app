package com.github.nsu_upprpo.school_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.github.nsu_upprpo.school_app.model.GradeDto;
import com.github.nsu_upprpo.school_app.model.GroupDto;
import com.github.nsu_upprpo.school_app.model.LessonDto;
import com.github.nsu_upprpo.school_app.model.ProjectDto;
import com.github.nsu_upprpo.school_app.storage.TeacherJournalStorage;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private TeacherJournalStorage journalStorage;
    private final Gson gson = new Gson();

    private List<GroupDto> currentGroups = new ArrayList<>();
    private List<ProjectDto> currentProjects = new ArrayList<>();
    private List<LessonDto> currentLessons = new ArrayList<>();
    private final List<StudentGradeItem> currentStudentGrades = new ArrayList<>();
    private int requestVersion;

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

        journalStorage = new TeacherJournalStorage(requireContext());
        submitButton.setOnClickListener(v -> sendChangedGrades());

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
        int requestId = nextRequestVersion();
        state = JournalState.COURSES;
        selectedGroup = null;
        selectedProject = null;
        selectedLesson = null;
        clearStudentGrades();
        updateBackCallback();

        titleText.setText("Журнал");
        subtitleText.setText("Выберите курс");
        submitButton.setVisibility(View.GONE);
        if (!showCachedCourses()) {
            showLoading("Загрузка курсов...");
        }

        groupApi.getTeacherGroups(authHeader).enqueue(new Callback<List<GroupDto>>() {
            @Override
            public void onResponse(Call<List<GroupDto>> call, Response<List<GroupDto>> response) {
                if (!isActiveRequest(requestId)) return;

                if (response.isSuccessful() && response.body() != null) {
                    currentGroups = response.body();
                    journalStorage.saveGroupsJson(gson.toJson(currentGroups));
                    showCoursesFromCache(false);
                } else {
                    showRetry("Не удалось загрузить курсы", v -> showCourses());
                }
            }

            @Override
            public void onFailure(Call<List<GroupDto>> call, Throwable t) {
                if (!isActiveRequest(requestId)) return;
                showRetry("Не удалось загрузить курсы. Проверьте интернет и попробуйте снова.", v -> showCourses());
            }
        });
    }

    private void showCoursesFromCache() {
        showCoursesFromCache(true);
    }

    private void showCoursesFromCache(boolean invalidateRequests) {
        if (invalidateRequests) {
            nextRequestVersion();
        }
        state = JournalState.COURSES;
        selectedGroup = null;
        selectedProject = null;
        selectedLesson = null;
        clearStudentGrades();
        updateBackCallback();

        titleText.setText("Журнал");
        subtitleText.setText("Выберите курс");
        submitButton.setVisibility(View.GONE);
        renderCourseCards(currentGroups);
    }

    private boolean showCachedCourses() {
        if (!journalStorage.hasGroups()) {
            return false;
        }

        Type type = new TypeToken<List<GroupDto>>() {}.getType();
        List<GroupDto> cachedGroups = parseList(journalStorage.getGroupsJson(), type);

        if (cachedGroups.isEmpty()) {
            return false;
        }

        currentGroups = cachedGroups;
        renderCourseCards(currentGroups);
        return true;
    }

    private void showProjects(GroupDto group) {
        int requestId = nextRequestVersion();
        state = JournalState.PROJECTS;
        selectedGroup = group;
        selectedProject = null;
        selectedLesson = null;
        currentProjects.clear();
        currentLessons.clear();
        clearStudentGrades();
        updateBackCallback();

        titleText.setText(safe(group.getCourseName(), "Курс"));
        subtitleText.setText("Выберите проект");
        submitButton.setVisibility(View.GONE);
        if (!showCachedProjects(group.getGroupId())) {
            showLoading("Загрузка проектов...");
        }
        refreshLessonsCacheForProjectCounts(group.getGroupId(), requestId);

        projectApi.getProjectsByGroup(authHeader, group.getGroupId()).enqueue(new Callback<List<ProjectDto>>() {
            @Override
            public void onResponse(Call<List<ProjectDto>> call, Response<List<ProjectDto>> response) {
                if (!isActiveRequest(requestId)) return;

                if (response.isSuccessful() && response.body() != null) {
                    currentProjects = response.body();
                    journalStorage.saveProjectsJson(group.getGroupId(), gson.toJson(currentProjects));
                    showProjectsFromCache(false);
                } else {
                    showRetry("Не удалось загрузить проекты", v -> showProjects(group));
                }
            }

            @Override
            public void onFailure(Call<List<ProjectDto>> call, Throwable t) {
                if (!isActiveRequest(requestId)) return;
                showRetry("Не удалось загрузить проекты. Проверьте интернет и попробуйте снова.", v -> showProjects(group));
            }
        });
    }

    private void refreshLessonsCacheForProjectCounts(String groupId, int requestId) {
        if (groupId == null || groupId.isEmpty()) {
            return;
        }

        scheduleApi.getGroupSchedule(authHeader, groupId).enqueue(new Callback<List<LessonDto>>() {
            @Override
            public void onResponse(Call<List<LessonDto>> call, Response<List<LessonDto>> response) {
                if (!isActiveRequest(requestId) || state != JournalState.PROJECTS) return;

                if (response.isSuccessful() && response.body() != null) {
                    journalStorage.saveLessonsJson(groupId, gson.toJson(response.body()));
                    if (currentProjects != null && !currentProjects.isEmpty()) {
                        renderProjectCards(currentProjects);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<LessonDto>> call, Throwable t) {
                // The project list can still work with project.totalLessons if lesson count refresh fails.
            }
        });
    }

    private void showProjectsFromCache() {
        showProjectsFromCache(true);
    }

    private void showProjectsFromCache(boolean invalidateRequests) {
        if (invalidateRequests) {
            nextRequestVersion();
        }
        state = JournalState.PROJECTS;
        selectedProject = null;
        selectedLesson = null;
        currentLessons.clear();
        clearStudentGrades();
        updateBackCallback();

        titleText.setText(safe(selectedGroup == null ? null : selectedGroup.getCourseName(), "Курс"));
        subtitleText.setText("Выберите проект");
        submitButton.setVisibility(View.GONE);
        renderProjectCards(currentProjects);
    }

    private boolean showCachedProjects(String groupId) {
        if (groupId == null || groupId.isEmpty() || !journalStorage.hasProjectsForGroup(groupId)) {
            return false;
        }

        Type type = new TypeToken<List<ProjectDto>>() {}.getType();
        List<ProjectDto> cachedProjects = parseList(journalStorage.getProjectsJson(groupId), type);

        if (cachedProjects.isEmpty()) {
            return false;
        }

        currentProjects = cachedProjects;
        renderProjectCards(currentProjects);
        return true;
    }

    private void showLessons(ProjectDto project) {
        int requestId = nextRequestVersion();
        state = JournalState.LESSONS;
        selectedProject = project;
        selectedLesson = null;
        currentLessons.clear();
        clearStudentGrades();
        updateBackCallback();

        titleText.setText(safe(project.getName(), "Проект"));
        subtitleText.setText("Выберите пару");
        submitButton.setVisibility(View.GONE);

        if (selectedGroup == null || selectedGroup.getGroupId() == null || selectedGroup.getGroupId().isEmpty()
                || project == null || project.getId() == null || project.getId().isEmpty()) {
            showEmpty("Выберите пару");
            return;
        }

        if (!showCachedLessons(project)) {
            showLoading("Загрузка пар...");
        }

        scheduleApi.getGroupSchedule(authHeader, selectedGroup.getGroupId()).enqueue(new Callback<List<LessonDto>>() {
            @Override
            public void onResponse(Call<List<LessonDto>> call, Response<List<LessonDto>> response) {
                if (!isActiveRequest(requestId)) return;

                if (response.isSuccessful() && response.body() != null) {
                    journalStorage.saveLessonsJson(selectedGroup.getGroupId(), gson.toJson(response.body()));
                    currentLessons = filterLessonsByProject(response.body(), project.getId());
                    showLessonsFromCache(false);
                } else {
                    showRetry("Не удалось загрузить пары", v -> showLessons(project));
                }
            }

            @Override
            public void onFailure(Call<List<LessonDto>> call, Throwable t) {
                if (!isActiveRequest(requestId)) return;
                showRetry("Не удалось загрузить пары. Проверьте интернет и попробуйте снова.", v -> showLessons(project));
            }
        });
    }

    private void showLessonsFromCache() {
        showLessonsFromCache(true);
    }

    private void showLessonsFromCache(boolean invalidateRequests) {
        if (invalidateRequests) {
            nextRequestVersion();
        }
        state = JournalState.LESSONS;
        selectedLesson = null;
        clearStudentGrades();
        updateBackCallback();

        titleText.setText(safe(selectedProject == null ? null : selectedProject.getName(), "Проект"));
        subtitleText.setText("Выберите пару");
        submitButton.setVisibility(View.GONE);
        renderLessonCards(currentLessons);
    }

    private boolean showCachedLessons(ProjectDto project) {
        if (selectedGroup == null || selectedGroup.getGroupId() == null || selectedGroup.getGroupId().isEmpty()
                || project == null || project.getId() == null || project.getId().isEmpty()
                || !journalStorage.hasLessonsForGroup(selectedGroup.getGroupId())) {
            return false;
        }

        Type type = new TypeToken<List<LessonDto>>() {}.getType();
        List<LessonDto> cachedLessons = parseList(journalStorage.getLessonsJson(selectedGroup.getGroupId()), type);

        if (cachedLessons.isEmpty()) {
            return false;
        }

        currentLessons = filterLessonsByProject(cachedLessons, project.getId());
        renderLessonCards(currentLessons);
        return true;
    }

    private void showStudents(LessonDto lesson) {
        int requestId = nextRequestVersion();
        state = JournalState.STUDENTS;
        selectedLesson = lesson;
        clearStudentGrades();
        updateBackCallback();

        titleText.setText(safe(lesson.getTopic(), "Пара"));
        subtitleText.setText(formatLessonTime(lesson));
        submitButton.setVisibility(View.VISIBLE);
        setSubmitEnabled(false);
        showLoading("Загрузка учеников...");

        if (selectedProject == null || selectedProject.getId() == null || selectedProject.getId().isEmpty()
                || lesson == null || lesson.getId() == null || lesson.getId().isEmpty()) {
            submitButton.setVisibility(View.GONE);
            showEmpty("Выберите пару");
            return;
        }

        attendanceApi.getLessonAttendances(authHeader, lesson.getId()).enqueue(new Callback<List<AttendanceDto>>() {
            @Override
            public void onResponse(Call<List<AttendanceDto>> call, Response<List<AttendanceDto>> response) {
                if (!isActiveRequest(requestId)) return;

                if (response.isSuccessful() && response.body() != null) {
                    loadProjectGrades(response.body(), requestId);
                } else {
                    submitButton.setVisibility(View.GONE);
                    showRetry("Не удалось загрузить список учеников", v -> showStudents(lesson));
                }
            }

            @Override
            public void onFailure(Call<List<AttendanceDto>> call, Throwable t) {
                if (!isActiveRequest(requestId)) return;
                submitButton.setVisibility(View.GONE);
                showRetry("Не удалось загрузить список учеников. Проверьте интернет и попробуйте снова.", v -> showStudents(lesson));
            }
        });
    }

    private void loadProjectGrades(List<AttendanceDto> attendances, int requestId) {
        if (selectedProject == null || selectedProject.getId() == null || selectedProject.getId().isEmpty()) {
            submitButton.setVisibility(View.GONE);
            showEmpty("Не удалось определить проект для оценок");
            return;
        }

        projectApi.getProjectGrades(authHeader, selectedProject.getId()).enqueue(new Callback<List<GradeDto>>() {
            @Override
            public void onResponse(Call<List<GradeDto>> call, Response<List<GradeDto>> response) {
                if (!isActiveRequest(requestId)) return;

                if (response.isSuccessful() && response.body() != null) {
                    renderStudentCards(attendances, response.body());
                } else {
                    submitButton.setVisibility(View.GONE);
                    showRetry("Не удалось загрузить оценки", v -> loadProjectGrades(attendances, nextRequestVersion()));
                }
            }

            @Override
            public void onFailure(Call<List<GradeDto>> call, Throwable t) {
                if (!isActiveRequest(requestId)) return;
                submitButton.setVisibility(View.GONE);
                showRetry("Не удалось загрузить оценки. Проверьте интернет и попробуйте снова.",
                        v -> loadProjectGrades(attendances, nextRequestVersion()));
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
            card.addView(createSubtitle("Занятий: " + getDisplayedLessonCount(project) + " • максимум " + project.getMaxScore() + " баллов"));
            card.addView(createAction("Открыть пары  ›"));
            card.setOnClickListener(v -> showLessons(project));
            journalContent.addView(card);
        }
    }

    private int getDisplayedLessonCount(ProjectDto project) {
        int cachedCount = getCachedLessonCount(project);
        if (cachedCount >= 0) {
            return cachedCount;
        }

        return project.getTotalLessons();
    }

    private int getCachedLessonCount(ProjectDto project) {
        if (selectedGroup == null || selectedGroup.getGroupId() == null || selectedGroup.getGroupId().isEmpty()
                || project == null || project.getId() == null || project.getId().isEmpty()
                || !journalStorage.hasLessonsForGroup(selectedGroup.getGroupId())) {
            return -1;
        }

        Type type = new TypeToken<List<LessonDto>>() {}.getType();
        List<LessonDto> cachedLessons = parseList(journalStorage.getLessonsJson(selectedGroup.getGroupId()), type);

        return filterLessonsByProject(cachedLessons, project.getId()).size();
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

    private void renderStudentCards(List<AttendanceDto> attendances, List<GradeDto> grades) {
        journalContent.removeAllViews();
        currentStudentGrades.clear();

        if (attendances == null || attendances.isEmpty()) {
            submitButton.setVisibility(View.GONE);
            showEmpty("Для этой пары пока нет учеников");
            return;
        }

        submitButton.setVisibility(View.VISIBLE);
        setSubmitEnabled(false);
        int maxScore = getSelectedProjectMaxScore();
        Map<String, GradeDto> gradesByChildId = mapGradesByChildId(grades);

        for (AttendanceDto attendance : attendances) {
            String childId = attendance.getChildId();
            if (childId == null || childId.isEmpty()) {
                continue;
            }

            GradeDto grade = gradesByChildId.get(childId);
            int initialScore = clampScore(grade == null || grade.getScore() == null ? 0 : grade.getScore(), maxScore);
            StudentGradeItem student = new StudentGradeItem(
                    childId,
                    safe(attendance.getChildName(), "Ученик"),
                    initialScore,
                    maxScore,
                    grade == null ? "" : safeRaw(grade.getComment())
            );
            currentStudentGrades.add(student);

            LinearLayout card = createCard();
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(android.view.Gravity.CENTER_VERTICAL);

            LinearLayout textBlock = new LinearLayout(requireContext());
            textBlock.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

            TextView nameText = createTitle(student.name);
            textBlock.addView(nameText);

            TextView gradeText = createGradeValue(student);
            TextView minusButton = createGradeButton("-");
            TextView plusButton = createGradeButton("+");
            updateGradeButtons(student, minusButton, plusButton);

            minusButton.setOnClickListener(v -> {
                if (student.score > student.minScore) {
                    student.score--;
                    gradeText.setText(formatGradeValue(student));
                    updateGradeButtons(student, minusButton, plusButton);
                    updateSubmitStateByGrades();
                }
            });

            plusButton.setOnClickListener(v -> {
                if (student.score < student.maxScore) {
                    student.score++;
                    gradeText.setText(formatGradeValue(student));
                    updateGradeButtons(student, minusButton, plusButton);
                    updateSubmitStateByGrades();
                }
            });

            card.addView(textBlock, textParams);
            card.addView(gradeText);
            card.addView(minusButton);
            card.addView(plusButton);
            journalContent.addView(card);
        }

        if (currentStudentGrades.isEmpty()) {
            submitButton.setVisibility(View.GONE);
            showEmpty("Для этой пары пока нет учеников");
        }
    }

    private Map<String, GradeDto> mapGradesByChildId(List<GradeDto> grades) {
        Map<String, GradeDto> result = new HashMap<>();

        if (grades == null) {
            return result;
        }

        for (GradeDto grade : grades) {
            if (grade == null || grade.getChildId() == null || grade.getChildId().isEmpty()) {
                continue;
            }

            result.put(grade.getChildId(), grade);
        }

        return result;
    }

    private void sendChangedGrades() {
        if (selectedProject == null || selectedProject.getId() == null || selectedProject.getId().isEmpty()) {
            Toast.makeText(requireContext(), "Не удалось определить проект", Toast.LENGTH_SHORT).show();
            return;
        }

        List<StudentGradeItem> changedGrades = getChangedGrades();
        if (changedGrades.isEmpty()) {
            setSubmitEnabled(false);
            return;
        }

        setSubmitEnabled(false);
        submitButton.setText("Отправка...");

        final int[] total = {changedGrades.size()};
        final int[] success = {0};
        final int[] errors = {0};

        for (StudentGradeItem student : changedGrades) {
            projectApi.saveProjectGrade(
                    authHeader,
                    selectedProject.getId(),
                    student.childId,
                    student.score,
                    student.comment
            ).enqueue(new Callback<GradeDto>() {
                @Override
                public void onResponse(Call<GradeDto> call, Response<GradeDto> response) {
                    if (!isAdded()) return;

                    if (response.isSuccessful()) {
                        success[0]++;
                    } else {
                        errors[0]++;
                    }

                    checkGradeSendingFinished(total[0], success[0], errors[0]);
                }

                @Override
                public void onFailure(Call<GradeDto> call, Throwable t) {
                    if (!isAdded()) return;

                    errors[0]++;
                    checkGradeSendingFinished(total[0], success[0], errors[0]);
                }
            });
        }
    }

    private void checkGradeSendingFinished(int total, int success, int errors) {
        if (success + errors < total) {
            return;
        }

        submitButton.setText("Отправить");

        if (errors == 0) {
            for (StudentGradeItem student : currentStudentGrades) {
                student.originalScore = student.score;
                student.minScore = student.score;
            }
            updateSubmitStateByGrades();
            Toast.makeText(requireContext(), "Оценки отправлены", Toast.LENGTH_SHORT).show();
        } else {
            updateSubmitStateByGrades();
            Toast.makeText(requireContext(), "Не удалось отправить все оценки", Toast.LENGTH_LONG).show();
        }
    }

    private List<StudentGradeItem> getChangedGrades() {
        List<StudentGradeItem> result = new ArrayList<>();

        for (StudentGradeItem student : currentStudentGrades) {
            if (student.score != student.originalScore) {
                result.add(student);
            }
        }

        return result;
    }

    private void updateSubmitStateByGrades() {
        setSubmitEnabled(!getChangedGrades().isEmpty());
    }

    private void setSubmitEnabled(boolean enabled) {
        submitButton.setEnabled(enabled);
        submitButton.setAlpha(enabled ? 1f : 0.4f);
    }

    private int getSelectedProjectMaxScore() {
        if (selectedProject == null) {
            return 0;
        }

        return Math.max(0, selectedProject.getMaxScore());
    }

    private int clampScore(int score, int maxScore) {
        return Math.max(0, Math.min(score, maxScore));
    }

    private String formatGradeValue(StudentGradeItem student) {
        return student.score + " / " + student.maxScore;
    }

    private void updateGradeButtons(StudentGradeItem student, TextView minusButton, TextView plusButton) {
        setGradeButtonEnabled(minusButton, student.score > student.minScore);
        setGradeButtonEnabled(plusButton, student.score < student.maxScore);
    }

    private void setGradeButtonEnabled(TextView button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.4f);
    }

    private void showLoading(String message) {
        journalContent.removeAllViews();
        submitButton.setVisibility(View.GONE);
        TextView textView = createSubtitle(message);
        journalContent.addView(textView);
    }

    private void showEmpty(String message) {
        journalContent.removeAllViews();
        setSubmitEnabled(false);
        TextView textView = createSubtitle(message);
        journalContent.addView(textView);
    }

    private void showRetry(String message, View.OnClickListener listener) {
        journalContent.removeAllViews();
        setSubmitEnabled(false);

        TextView textView = createSubtitle(message);
        journalContent.addView(textView);

        Button retryButton = new Button(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(14), 0, 0);
        retryButton.setLayoutParams(params);
        retryButton.setText("Повторить");
        retryButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        retryButton.setBackgroundResource(R.drawable.bg_button_orange);
        retryButton.setOnClickListener(listener);
        journalContent.addView(retryButton);
    }

    private int nextRequestVersion() {
        requestVersion++;
        return requestVersion;
    }

    private boolean isActiveRequest(int requestId) {
        return isAdded() && requestId == requestVersion;
    }

    private void clearStudentGrades() {
        currentStudentGrades.clear();
        setSubmitEnabled(false);
    }

    private <T> List<T> parseList(String json, Type type) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<T> result = gson.fromJson(json, type);
            return result == null ? new ArrayList<>() : result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
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

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(32), dp(32));
        params.setMargins(dp(4), 0, 0, 0);
        textView.setLayoutParams(params);

        return textView;
    }

    private TextView createGradeValue(StudentGradeItem student) {
        TextView textView = new TextView(requireContext());
        textView.setText(formatGradeValue(student));
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
        textView.setTextSize(14);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        textView.setSingleLine(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(82), dp(34));
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

    private String safeRaw(String value) {
        return value == null ? "" : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class StudentGradeItem {
        final String childId;
        final String name;
        final int maxScore;
        final String comment;
        int minScore;
        int score;
        int originalScore;

        StudentGradeItem(String childId, String name, int score, int maxScore, String comment) {
            this.childId = childId;
            this.name = name;
            this.score = score;
            this.originalScore = score;
            this.maxScore = maxScore;
            this.comment = comment;
            this.minScore = score;
        }
    }
}
