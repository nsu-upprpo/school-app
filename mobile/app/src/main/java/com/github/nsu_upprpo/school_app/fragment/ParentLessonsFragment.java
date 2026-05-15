package com.github.nsu_upprpo.school_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.adapter.ParentLessonAdapter;
import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.AttendanceApi;
import com.github.nsu_upprpo.school_app.api.ChildApi;
import com.github.nsu_upprpo.school_app.api.GroupApi;
import com.github.nsu_upprpo.school_app.api.ScheduleApi;
import com.github.nsu_upprpo.school_app.model.AttendanceDto;
import com.github.nsu_upprpo.school_app.model.ChildDto;
import com.github.nsu_upprpo.school_app.model.GroupDto;
import com.github.nsu_upprpo.school_app.model.LessonDto;
import com.github.nsu_upprpo.school_app.model.ParentLessonItem;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;
import com.github.nsu_upprpo.school_app.storage.ParentLessonsStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentLessonsFragment extends Fragment {

    private TextView parentFutureTab;
    private TextView parentMissedTab;
    private RecyclerView parentLessonsRecyclerView;
    private TextView emptyLessonsText;

    private TextView mondayDot;
    private TextView tuesdayDot;
    private TextView wednesdayDot;
    private TextView thursdayDot;
    private TextView fridayDot;
    private TextView saturdayDot;
    private TextView sundayDot;

    private ParentLessonAdapter adapter;

    private final List<ParentLessonItem> futureLessons = new ArrayList<>();
    private final List<ParentLessonItem> missedLessons = new ArrayList<>();

    private final Map<String, GroupDto> groupsById = new HashMap<>();
    private final Map<String, String> childNamesByGroupId = new HashMap<>();

    private boolean isFutureMode = true;

    private View mondayContainer;
    private View tuesdayContainer;
    private View wednesdayContainer;
    private View thursdayContainer;
    private View fridayContainer;
    private View saturdayContainer;
    private View sundayContainer;

    private TextView mondayDateText;
    private TextView tuesdayDateText;
    private TextView wednesdayDateText;
    private TextView thursdayDateText;
    private TextView fridayDateText;
    private TextView saturdayDateText;
    private TextView sundayDateText;

    private String authHeader;

    private ParentLessonsStorage lessonsStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_parent_lessons, container, false);

        parentFutureTab = view.findViewById(R.id.parentFutureTab);
        parentMissedTab = view.findViewById(R.id.parentMissedTab);
        parentLessonsRecyclerView = view.findViewById(R.id.parentLessonsRecyclerView);
        emptyLessonsText = view.findViewById(R.id.emptyLessonsText);

        mondayDot = view.findViewById(R.id.mondayDot);
        tuesdayDot = view.findViewById(R.id.tuesdayDot);
        wednesdayDot = view.findViewById(R.id.wednesdayDot);
        thursdayDot = view.findViewById(R.id.thursdayDot);
        fridayDot = view.findViewById(R.id.fridayDot);
        saturdayDot = view.findViewById(R.id.saturdayDot);
        sundayDot = view.findViewById(R.id.sundayDot);

        mondayContainer = view.findViewById(R.id.mondayContainer);
        tuesdayContainer = view.findViewById(R.id.tuesdayContainer);
        wednesdayContainer = view.findViewById(R.id.wednesdayContainer);
        thursdayContainer = view.findViewById(R.id.thursdayContainer);
        fridayContainer = view.findViewById(R.id.fridayContainer);
        saturdayContainer = view.findViewById(R.id.saturdayContainer);
        sundayContainer = view.findViewById(R.id.sundayContainer);

        mondayDateText = view.findViewById(R.id.mondayDateText);
        tuesdayDateText = view.findViewById(R.id.tuesdayDateText);
        wednesdayDateText = view.findViewById(R.id.wednesdayDateText);
        thursdayDateText = view.findViewById(R.id.thursdayDateText);
        fridayDateText = view.findViewById(R.id.fridayDateText);
        saturdayDateText = view.findViewById(R.id.saturdayDateText);
        sundayDateText = view.findViewById(R.id.sundayDateText);

        adapter = new ParentLessonAdapter();
        lessonsStorage = new ParentLessonsStorage(requireContext());
        parentLessonsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        parentLessonsRecyclerView.setAdapter(adapter);

        adapter.setOnLessonClickListener(this::showLessonDialog);

        parentFutureTab.setOnClickListener(v -> {
            isFutureMode = true;
            setFutureTabActive();
            showFutureLessons();
        });

        parentMissedTab.setOnClickListener(v -> {
            isFutureMode = false;
            setMissedTabActive();
            showMissedLessons();
        });

        setFutureTabActive();
        setupCurrentWeekDates();
        loadParentLessons();

        return view;
    }

    private void loadParentLessons() {
        if (lessonsStorage.hasAnyLessons()) {
            futureLessons.clear();
            futureLessons.addAll(lessonsStorage.getFutureLessons());

            missedLessons.clear();
            missedLessons.addAll(lessonsStorage.getMissedLessons());

            updateWeekDots();

            if (isFutureMode) {
                showFutureLessons();
            } else {
                showMissedLessons();
            }

            return;
        }

        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            showEmpty("Не удалось загрузить занятия");
            return;
        }

        authHeader = "Bearer " + token;

        futureLessons.clear();
        missedLessons.clear();
        groupsById.clear();
        childNamesByGroupId.clear();

        loadChildren();
    }

    private void loadChildren() {
        ChildApi childApi = ApiClient.getClient().create(ChildApi.class);

        childApi.getMyChildren(authHeader).enqueue(new Callback<List<ChildDto>>() {
            @Override
            public void onResponse(Call<List<ChildDto>> call, Response<List<ChildDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    buildChildNamesByGroup(response.body());
                    loadParentGroups();
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить детей", Toast.LENGTH_SHORT).show();
                    loadParentGroups();
                }
            }

            @Override
            public void onFailure(Call<List<ChildDto>> call, Throwable t) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(), "Ошибка загрузки детей: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadParentGroups();
            }
        });
    }

    private void buildChildNamesByGroup(List<ChildDto> children) {
        Map<String, List<String>> temp = new HashMap<>();

        for (ChildDto child : children) {
            if (child.getGroups() == null) {
                continue;
            }

            String childName = safe(child.getFullName());

            for (GroupDto group : child.getGroups()) {
                String groupId = group.getGroupId();

                if (groupId == null || groupId.isEmpty()) {
                    continue;
                }

                if (!temp.containsKey(groupId)) {
                    temp.put(groupId, new ArrayList<>());
                }

                temp.get(groupId).add(childName);
            }
        }

        for (Map.Entry<String, List<String>> entry : temp.entrySet()) {
            childNamesByGroupId.put(entry.getKey(), joinNames(entry.getValue()));
        }
    }

    private void loadParentGroups() {
        GroupApi groupApi = ApiClient.getClient().create(GroupApi.class);

        groupApi.getParentGroups(authHeader).enqueue(new Callback<List<GroupDto>>() {
            @Override
            public void onResponse(Call<List<GroupDto>> call, Response<List<GroupDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<GroupDto> groups = response.body();

                    if (groups.isEmpty()) {
                        showEmpty("У вас пока нет занятий");
                        return;
                    }

                    for (GroupDto group : groups) {
                        groupsById.put(group.getGroupId(), group);
                    }

                    loadSchedulesForGroups(groups);
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить группы", Toast.LENGTH_SHORT).show();
                    showEmpty("Не удалось загрузить занятия");
                }
            }

            @Override
            public void onFailure(Call<List<GroupDto>> call, Throwable t) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(), "Ошибка загрузки групп: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmpty("Не удалось загрузить занятия");
            }
        });
    }

    private void loadSchedulesForGroups(List<GroupDto> groups) {
        ScheduleApi scheduleApi = ApiClient.getClient().create(ScheduleApi.class);

        final int[] loadedCount = {0};

        for (GroupDto group : groups) {
            String groupId = group.getGroupId();

            if (groupId == null || groupId.isEmpty()) {
                loadedCount[0]++;
                continue;
            }

            scheduleApi.getGroupSchedule(authHeader, groupId).enqueue(new Callback<List<LessonDto>>() {
                @Override
                public void onResponse(Call<List<LessonDto>> call, Response<List<LessonDto>> response) {
                    if (!isAdded()) return;

                    if (response.isSuccessful() && response.body() != null) {
                        addLessonsFromSchedule(group, response.body());
                    }

                    loadedCount[0]++;
                    if (loadedCount[0] == groups.size()) {
                        finishLoadingLessons();
                    }
                }

                @Override
                public void onFailure(Call<List<LessonDto>> call, Throwable t) {
                    if (!isAdded()) return;

                    loadedCount[0]++;
                    if (loadedCount[0] == groups.size()) {
                        finishLoadingLessons();
                    }
                }
            });
        }
    }

    private void addLessonsFromSchedule(GroupDto group, List<LessonDto> lessons) {
        long now = System.currentTimeMillis();

        for (LessonDto lesson : lessons) {
            long dateMillis = parseDateMillis(lesson.getStartTime());

            if (dateMillis == 0) {
                continue;
            }

            ParentLessonItem item = new ParentLessonItem(
                    lesson.getId(),
                    lesson.getGroupId(),
                    safe(group.getCourseName()),
                    safe(group.getTeacherName()),
                    safe(lesson.getTopic()),
                    safe(childNamesByGroupId.get(group.getGroupId())),
                    formatTime(lesson.getStartTime()),
                    formatTime(lesson.getEndTime()),
                    dateMillis,
                    safe(lesson.getStatus())
            );

            futureLessons.add(item);
        }
    }

    private void setupCurrentWeekDates() {
        Calendar calendar = Calendar.getInstance();

        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

        int diff;

        if (currentDay == Calendar.SUNDAY) {
            diff = -6;
        } else {
            diff = Calendar.MONDAY - currentDay;
        }

        calendar.add(Calendar.DAY_OF_MONTH, diff);

        TextView[] dateViews = {
                mondayDateText,
                tuesdayDateText,
                wednesdayDateText,
                thursdayDateText,
                fridayDateText,
                saturdayDateText,
                sundayDateText
        };

        SimpleDateFormat format =
                new SimpleDateFormat("d", new Locale("ru"));

        for (TextView textView : dateViews) {
            textView.setText(format.format(calendar.getTime()).toLowerCase());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void finishLoadingLessons() {
        Collections.sort(futureLessons, Comparator.comparingLong(ParentLessonItem::getDateMillis));

        if (futureLessons.size() > 3) {
            List<ParentLessonItem> nearestThree = new ArrayList<>(futureLessons.subList(0, 3));
            futureLessons.clear();
            futureLessons.addAll(nearestThree);
        }

        updateWeekDots();

        if (isFutureMode) {
            showFutureLessons();
        }

        loadMissedLessons();
        lessonsStorage.saveFutureLessons(futureLessons);
    }

    private void loadMissedLessons() {
        AttendanceApi attendanceApi = ApiClient.getClient().create(AttendanceApi.class);

        List<ParentLessonItem> lessonsForAttendance = new ArrayList<>();

        for (ParentLessonItem lesson : futureLessons) {
            lessonsForAttendance.add(lesson);
        }

        if (lessonsForAttendance.isEmpty()) {
            return;
        }

        final int[] loadedCount = {0};

        for (ParentLessonItem lesson : lessonsForAttendance) {
            attendanceApi.getLessonAttendances(authHeader, lesson.getLessonId())
                    .enqueue(new Callback<List<AttendanceDto>>() {
                        @Override
                        public void onResponse(Call<List<AttendanceDto>> call,
                                               Response<List<AttendanceDto>> response) {
                            if (!isAdded()) return;

                            if (response.isSuccessful() && response.body() != null) {
                                addMissedLessonsFromAttendance(lesson, response.body());
                            }

                            loadedCount[0]++;
                            if (loadedCount[0] == lessonsForAttendance.size() && !isFutureMode) {
                                showMissedLessons();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<AttendanceDto>> call, Throwable t) {
                            if (!isAdded()) return;

                            loadedCount[0]++;
                            if (loadedCount[0] == lessonsForAttendance.size() && !isFutureMode) {
                                showMissedLessons();
                            }
                        }
                    });
        }
    }

    private void addMissedLessonsFromAttendance(ParentLessonItem lesson,
                                                List<AttendanceDto> attendances) {
        for (AttendanceDto attendance : attendances) {
            if (!"ABSENT".equalsIgnoreCase(attendance.getStatus())) {
                continue;
            }

            ParentLessonItem missedItem = new ParentLessonItem(
                    lesson.getLessonId(),
                    lesson.getGroupId(),
                    lesson.getCourseName(),
                    lesson.getTeacherName(),
                    lesson.getTopic(),
                    safe(attendance.getChildName()),
                    lesson.getStartTime(),
                    lesson.getEndTime(),
                    lesson.getDateMillis(),
                    "ABSENT"
            );

            missedLessons.add(missedItem);
        }

        Collections.sort(missedLessons, Comparator.comparingLong(ParentLessonItem::getDateMillis));
        lessonsStorage.saveMissedLessons(missedLessons);
    }

    private void showFutureLessons() {
        if (futureLessons.isEmpty()) {
            showEmpty("Ближайших занятий пока нет");
        } else {
            showList(futureLessons);
        }
    }

    private void showMissedLessons() {
        if (missedLessons.isEmpty()) {
            showEmpty("Пропусков пока нет");
        } else {
            showList(missedLessons);
        }
    }

    private void showList(List<ParentLessonItem> lessons) {
        adapter.updateLessons(lessons);
        emptyLessonsText.setVisibility(View.GONE);
        parentLessonsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmpty(String message) {
        adapter.updateLessons(null);
        emptyLessonsText.setText(message);
        emptyLessonsText.setVisibility(View.VISIBLE);
        parentLessonsRecyclerView.setVisibility(View.GONE);
    }

    private void showLessonDialog(ParentLessonItem lesson) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_parent_lesson_actions, null);

        TextView lessonDateText = view.findViewById(R.id.lessonDateText);
        TextView lessonTimeText = view.findViewById(R.id.lessonTimeText);
        TextView lessonStatusText = view.findViewById(R.id.lessonStatusText);
        TextView lessonCourseText = view.findViewById(R.id.lessonCourseText);
        TextView lessonTopicText = view.findViewById(R.id.lessonTopicText);
        TextView lessonTeacherText = view.findViewById(R.id.lessonTeacherText);

        View moveLessonAction = view.findViewById(R.id.moveLessonAction);
        View cancelLessonAction = view.findViewById(R.id.cancelLessonAction);

        TextView moveLessonText = moveLessonAction.findViewById(R.id.actionText);
        ImageView moveLessonIcon = moveLessonAction.findViewById(R.id.actionIcon);
        TextView cancelLessonText = cancelLessonAction.findViewById(R.id.actionText);
        ImageView cancelLessonIcon = cancelLessonAction.findViewById(R.id.actionIcon);

        moveLessonText.setText("Перенести занятие");
        moveLessonIcon.setImageResource(R.drawable.ic_edit);
        cancelLessonText.setText("Отменить занятие");
        cancelLessonIcon.setImageResource(R.drawable.ic_close);

        lessonDateText.setText(formatDateForCard(lesson.getDateMillis()));
        lessonTimeText.setText(lesson.getStartTime() + "–" + lesson.getEndTime());
        lessonCourseText.setText("Курс: " + safe(lesson.getCourseName()));
        lessonTopicText.setText("Тема: " + safe(lesson.getTopic()));
        lessonTeacherText.setText("Преподаватель: " + safe(lesson.getTeacherName()));

        if ("ABSENT".equalsIgnoreCase(lesson.getStatus())) {
            lessonStatusText.setVisibility(View.VISIBLE);
            lessonStatusText.setText("Пропущено");
        } else {
            lessonStatusText.setVisibility(View.GONE);
        }

        moveLessonAction.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Перенос занятия пока не реализован", Toast.LENGTH_SHORT).show()
        );

        cancelLessonAction.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Отмена занятия пока не реализована", Toast.LENGTH_SHORT).show()
        );

        dialog.setContentView(view);
        dialog.show();
    }

    private String formatDateForCard(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("d MMMM (EEEE)", new Locale("ru"));
        return format.format(new Date(millis));
    }

    private void setFutureTabActive() {
        parentFutureTab.setBackgroundResource(R.drawable.bg_button_orange);
        parentFutureTab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        parentMissedTab.setBackgroundResource(R.drawable.bg_tab_inactive);
        parentMissedTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
    }

    private void setMissedTabActive() {
        parentMissedTab.setBackgroundResource(R.drawable.bg_button_orange);
        parentMissedTab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        parentFutureTab.setBackgroundResource(R.drawable.bg_tab_inactive);
        parentFutureTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
    }

    private void updateWeekDots() {
        hideAllDots();

        for (ParentLessonItem lesson : futureLessons) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(lesson.getDateMillis());

            int day = calendar.get(Calendar.DAY_OF_WEEK);

            if (day == Calendar.MONDAY) {
                mondayDot.setVisibility(View.VISIBLE);
            }
            if (day == Calendar.TUESDAY) {
                tuesdayDot.setVisibility(View.VISIBLE);
            }
            if (day == Calendar.WEDNESDAY) {
                wednesdayDot.setVisibility(View.VISIBLE);
            }
            if (day == Calendar.THURSDAY) {
                thursdayDot.setVisibility(View.VISIBLE);
            }
            if (day == Calendar.FRIDAY) {
                fridayDot.setVisibility(View.VISIBLE);
            }
            if (day == Calendar.SATURDAY) {
                saturdayDot.setVisibility(View.VISIBLE);
            }
            if (day == Calendar.SUNDAY) {
                sundayDot.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideAllDots() {
        mondayDot.setVisibility(View.INVISIBLE);
        tuesdayDot.setVisibility(View.INVISIBLE);
        wednesdayDot.setVisibility(View.INVISIBLE);
        thursdayDot.setVisibility(View.INVISIBLE);
        fridayDot.setVisibility(View.INVISIBLE);
        saturdayDot.setVisibility(View.INVISIBLE);
        sundayDot.setVisibility(View.INVISIBLE);
    }

    private long parseDateMillis(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return 0;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        try {
            Date date = format.parse(isoDateTime);
            return date == null ? 0 : date.getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    private String formatTime(String isoDateTime) {
        long millis = parseDateMillis(isoDateTime);

        if (millis == 0) {
            return "";
        }

        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return format.format(new Date(millis));
    }

    private String joinNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (String name : names) {
            if (name == null || name.isEmpty()) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append(", ");
            }

            sb.append(name);
        }

        return sb.toString();
    }

    private String safe(String value) {
        return value == null || value.isEmpty() ? "не указано" : value;
    }
}
