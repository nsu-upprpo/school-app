package com.github.nsu_upprpo.school_app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.github.nsu_upprpo.school_app.api.ParentLessonActionApi;
import com.github.nsu_upprpo.school_app.api.ScheduleApi;
import com.github.nsu_upprpo.school_app.model.AttendanceDto;
import com.github.nsu_upprpo.school_app.model.CancelLessonRequest;
import com.github.nsu_upprpo.school_app.model.ChildDto;
import com.github.nsu_upprpo.school_app.model.GroupDto;
import com.github.nsu_upprpo.school_app.model.LessonDto;
import com.github.nsu_upprpo.school_app.model.ParentLessonItem;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;
import com.github.nsu_upprpo.school_app.storage.ParentLessonsStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.IOException;
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

    private static final String TAG = "ParentLessons";

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
    private final List<ParentLessonItem> loadedLessons = new ArrayList<>();

    private final Map<String, GroupDto> groupsById = new HashMap<>();

    private boolean isFutureMode = true;
    private boolean showAllFutureLessons;
    private boolean showAllMissedLessons;
    private boolean showChildNameInLessonCards;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().setFragmentResultListener(
                ParentLessonRescheduleFragment.RESULT_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    if (bundle.getBoolean(ParentLessonRescheduleFragment.RESULT_REFRESH, false)) {
                        lessonsStorage.clear();
                        refreshParentLessonsFromBackend();
                    }
                }
        );
    }

    private void loadParentLessons() {
        if (lessonsStorage.hasAnyLessons()) {
            showCachedLessons();

            if (hasInvalidActionIds(futureLessons) || hasInvalidActionIds(missedLessons)) {
                lessonsStorage.clear();
                refreshParentLessonsFromBackend();
                return;
            }

            updateWeekDots();

            if (isFutureMode) {
                showFutureLessons();
            } else {
                showMissedLessons();
            }

            refreshParentLessonsFromBackend();
            return;
        }

        refreshParentLessonsFromBackend();
    }

    private boolean showCachedLessons() {
        if (!lessonsStorage.hasAnyLessons()) {
            return false;
        }

        futureLessons.clear();
        futureLessons.addAll(lessonsStorage.getFutureLessons());

        missedLessons.clear();
        missedLessons.addAll(lessonsStorage.getMissedLessons());

        showChildNameInLessonCards = hasMultipleChildrenInLessons(futureLessons, missedLessons);
        adapter.setShowChildName(showChildNameInLessonCards);

        updateWeekDots();

        if (isFutureMode) {
            showFutureLessons();
        } else {
            showMissedLessons();
        }

        return true;
    }

    private void refreshParentLessonsFromBackend() {

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
        loadedLessons.clear();
        groupsById.clear();
        showAllFutureLessons = false;
        showAllMissedLessons = false;

        loadChildren();
    }

    private void loadChildren() {
        ChildApi childApi = ApiClient.getClient().create(ChildApi.class);

        childApi.getMyChildren(authHeader).enqueue(new Callback<List<ChildDto>>() {
            @Override
            public void onResponse(Call<List<ChildDto>> call, Response<List<ChildDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<ChildDto> children = response.body();
                    showChildNameInLessonCards = children.size() > 1;
                    adapter.setShowChildName(showChildNameInLessonCards);
                    buildGroupsById(children);
                    loadParentGroupsThenSchedules(children);
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить детей", Toast.LENGTH_SHORT).show();
                    showBackendErrorOrCache("Не удалось загрузить занятия");
                }
            }

            @Override
            public void onFailure(Call<List<ChildDto>> call, Throwable t) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(), "Ошибка загрузки детей: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showBackendErrorOrCache("Не удалось загрузить занятия");
            }
        });
    }

    private void buildGroupsById(List<ChildDto> children) {
        groupsById.clear();
        for (ChildDto child : children) {
            if (child.getGroups() == null) {
                continue;
            }

            for (GroupDto group : child.getGroups()) {
                String groupId = group.getGroupId();
                if (groupId == null || groupId.isEmpty()) {
                    continue;
                }

                groupsById.put(groupId, group);
            }
        }
    }

    private void loadParentGroupsThenSchedules(List<ChildDto> children) {
        GroupApi groupApi = ApiClient.getClient().create(GroupApi.class);

        groupApi.getParentGroups(authHeader).enqueue(new Callback<List<GroupDto>>() {
            @Override
            public void onResponse(Call<List<GroupDto>> call, Response<List<GroupDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    mergeParentGroups(response.body());
                } else {
                    Log.e(TAG, "parent groups failed code=" + response.code()
                            + ", body=" + getErrorBodyString(response));
                }

                loadSchedulesForChildren(children);
            }

            @Override
            public void onFailure(Call<List<GroupDto>> call, Throwable t) {
                if (!isAdded()) return;

                Log.e(TAG, "parent groups error message=" + t.getMessage());
                loadSchedulesForChildren(children);
            }
        });
    }

    private void mergeParentGroups(List<GroupDto> groups) {
        for (GroupDto group : groups) {
            if (group == null || isEmpty(group.getGroupId())) {
                continue;
            }

            groupsById.put(group.getGroupId(), group);
        }
    }

    private void loadSchedulesForChildren(List<ChildDto> children) {
        if (children == null || children.isEmpty()) {
            showEmpty("У вас пока нет занятий");
            return;
        }

        ScheduleApi scheduleApi = ApiClient.getClient().create(ScheduleApi.class);

        final int[] loadedCount = {0};
        final boolean[] hasScheduleError = {false};
        String from = "2025-01-01T00:00:00";
        String to = "2030-01-01T00:00:00";

        for (ChildDto child : children) {
            String childId = child.getId();

            if (childId == null || childId.isEmpty()) {
                hasScheduleError[0] = true;
                loadedCount[0]++;
                if (loadedCount[0] == children.size()) {
                    finishLoadingLessons(false);
                }
                continue;
            }

            scheduleApi.getChildSchedule(authHeader, childId, from, to).enqueue(new Callback<List<LessonDto>>() {
                @Override
                public void onResponse(Call<List<LessonDto>> call, Response<List<LessonDto>> response) {
                    if (!isAdded()) return;

                    if (response.isSuccessful() && response.body() != null) {
                        addLessonsFromChildSchedule(child, response.body());
                    } else {
                        Log.e(TAG, "child schedule failed childId=" + childId
                                + ", code=" + response.code()
                                + ", body=" + getErrorBodyString(response));
                        hasScheduleError[0] = true;
                    }

                    loadedCount[0]++;
                    if (loadedCount[0] == children.size()) {
                        finishLoadingLessons(!hasScheduleError[0]);
                    }
                }

                @Override
                public void onFailure(Call<List<LessonDto>> call, Throwable t) {
                    if (!isAdded()) return;

                    Log.e(TAG, "child schedule error childId=" + childId + ", message=" + t.getMessage());
                    hasScheduleError[0] = true;
                    loadedCount[0]++;
                    if (loadedCount[0] == children.size()) {
                        finishLoadingLessons(false);
                    }
                }
            });
        }
    }

    private void addLessonsFromChildSchedule(ChildDto child, List<LessonDto> lessons) {
        long now = System.currentTimeMillis();

        for (LessonDto lesson : lessons) {
            long dateMillis = parseDateMillis(lesson.getStartTime());

            if (dateMillis == 0) {
                continue;
            }

            GroupDto group = groupsById.get(lesson.getGroupId());
            ParentLessonItem item = createParentLessonItem(
                    group,
                    lesson,
                    safeRaw(child.getId()),
                    safe(child.getFullName()),
                    dateMillis
            );

            loadedLessons.add(item);

            if (dateMillis > now && !isSchoolCancelled(item) && shouldShowInFutureList(lesson.getChildStatus())) {
                futureLessons.add(item);
            }
        }
    }

    private boolean hasInvalidActionIds(List<ParentLessonItem> lessons) {
        if (lessons == null) {
            return false;
        }

        for (ParentLessonItem lesson : lessons) {
            if (lesson == null) {
                continue;
            }

            if (lesson.getLessonId() == null || lesson.getLessonId().isEmpty()
                    || lesson.getChildId() == null || lesson.getChildId().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @SafeVarargs
    private final boolean hasMultipleChildrenInLessons(List<ParentLessonItem>... lessonLists) {
        List<String> childIds = new ArrayList<>();

        for (List<ParentLessonItem> lessons : lessonLists) {
            if (lessons == null) {
                continue;
            }

            for (ParentLessonItem lesson : lessons) {
                if (lesson == null || isEmpty(lesson.getChildId())) {
                    continue;
                }

                if (!childIds.contains(lesson.getChildId())) {
                    childIds.add(lesson.getChildId());
                }

                if (childIds.size() > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    private ParentLessonItem createParentLessonItem(GroupDto group, LessonDto lesson,
                                                    String childId, String childName,
                                                    long dateMillis) {
        String lessonId = safeRaw(lesson.getId());

        return new ParentLessonItem(
                lessonId,
                childId,
                lesson.getGroupId(),
                group == null ? "не указано" : safe(group.getCourseName()),
                getTeacherName(lesson, group),
                safe(lesson.getTopic()),
                childName,
                formatTime(lesson.getStartTime()),
                formatTime(lesson.getEndTime()),
                dateMillis,
                safe(lesson.getStatus()),
                lesson.getChildStatus(),
                lesson.getRescheduledFromLessonId(),
                lesson.getRescheduledToLessonId()
        );
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

    private void finishLoadingLessons(boolean canSaveToCache) {
        Collections.sort(futureLessons, Comparator.comparingLong(ParentLessonItem::getDateMillis));

        updateWeekDots();

        if (isFutureMode) {
            showFutureLessons();
        }

        loadMissedLessons(canSaveToCache);

        if (canSaveToCache) {
            lessonsStorage.saveFutureLessons(futureLessons);
        }
    }

    private void loadMissedLessons(boolean canSaveToCache) {
        AttendanceApi attendanceApi = ApiClient.getClient().create(AttendanceApi.class);

        List<ParentLessonItem> lessonsForAttendance = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (ParentLessonItem lesson : loadedLessons) {
            if (isCancelledByParent(lesson)) {
                missedLessons.add(new ParentLessonItem(
                        lesson.getLessonId(),
                        lesson.getChildId(),
                        lesson.getGroupId(),
                        lesson.getCourseName(),
                        lesson.getTeacherName(),
                        lesson.getTopic(),
                        lesson.getChildNames(),
                        lesson.getStartTime(),
                        lesson.getEndTime(),
                        lesson.getDateMillis(),
                        "CANCELLED_BY_PARENT",
                        lesson.getChildStatus(),
                        lesson.getRescheduledFromLessonId(),
                        lesson.getRescheduledToLessonId()
                ));
                continue;
            }

            if (isAbsentLesson(lesson)) {
                missedLessons.add(new ParentLessonItem(
                        lesson.getLessonId(),
                        lesson.getChildId(),
                        lesson.getGroupId(),
                        lesson.getCourseName(),
                        lesson.getTeacherName(),
                        lesson.getTopic(),
                        lesson.getChildNames(),
                        lesson.getStartTime(),
                        lesson.getEndTime(),
                        lesson.getDateMillis(),
                        "ABSENT",
                        lesson.getChildStatus(),
                        lesson.getRescheduledFromLessonId(),
                        lesson.getRescheduledToLessonId()
                ));
                continue;
            }

            if (lesson.getDateMillis() < now) {
                lessonsForAttendance.add(lesson);
            }
        }

        if (lessonsForAttendance.isEmpty()) {
            if (canSaveToCache) {
                lessonsStorage.saveMissedLessons(missedLessons);
            }
            return;
        }

        final int[] loadedCount = {0};
        final boolean[] hasAttendanceError = {false};

        for (ParentLessonItem lesson : lessonsForAttendance) {
            attendanceApi.getLessonAttendances(authHeader, lesson.getLessonId())
                    .enqueue(new Callback<List<AttendanceDto>>() {
                        @Override
                        public void onResponse(Call<List<AttendanceDto>> call,
                                               Response<List<AttendanceDto>> response) {
                            if (!isAdded()) return;

                            if (response.isSuccessful() && response.body() != null) {
                                addMissedLessonsFromAttendance(lesson, response.body());
                            } else {
                                hasAttendanceError[0] = true;
                            }

                            loadedCount[0]++;
                            if (loadedCount[0] == lessonsForAttendance.size()) {
                                finishLoadingMissedLessons(canSaveToCache && !hasAttendanceError[0]);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<AttendanceDto>> call, Throwable t) {
                            if (!isAdded()) return;

                            hasAttendanceError[0] = true;
                            loadedCount[0]++;
                            if (loadedCount[0] == lessonsForAttendance.size()) {
                                finishLoadingMissedLessons(false);
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

            if (!safeRaw(attendance.getChildId()).equals(lesson.getChildId())) {
                continue;
            }

            ParentLessonItem missedItem = new ParentLessonItem(
                    lesson.getLessonId(),
                    safeRaw(attendance.getChildId()),
                    lesson.getGroupId(),
                    lesson.getCourseName(),
                    lesson.getTeacherName(),
                    lesson.getTopic(),
                    safe(attendance.getChildName()),
                    lesson.getStartTime(),
                    lesson.getEndTime(),
                    lesson.getDateMillis(),
                    "ABSENT",
                    lesson.getChildStatus(),
                    lesson.getRescheduledFromLessonId(),
                    lesson.getRescheduledToLessonId()
            );

            missedLessons.add(missedItem);
        }

        Collections.sort(missedLessons, Comparator.comparingLong(ParentLessonItem::getDateMillis));
    }

    private void finishLoadingMissedLessons(boolean canSaveToCache) {
        Collections.sort(missedLessons, Comparator.comparingLong(ParentLessonItem::getDateMillis));

        if (!isFutureMode) {
            showMissedLessons();
        }

        if (canSaveToCache) {
            lessonsStorage.saveMissedLessons(missedLessons);
        }
    }

    private void showBackendErrorOrCache(String message) {
        if (!showCachedLessons()) {
            showEmpty(message);
        }
    }

    private void showFutureLessons() {
        if (futureLessons.isEmpty()) {
            showEmpty("Ближайших занятий пока нет");
        } else {
            showList(futureLessons, showAllFutureLessons);
        }
    }

    private void showMissedLessons() {
        if (missedLessons.isEmpty()) {
            showEmpty("Пропусков пока нет");
        } else {
            showList(missedLessons, showAllMissedLessons);
        }
    }

    private void showList(List<ParentLessonItem> lessons, boolean showAll) {
        boolean showFooter = lessons != null && lessons.size() > 3;
        adapter.updateLessons(
                getVisibleLessons(lessons, showAll),
                showFooter,
                showAll,
                v -> toggleShowAllLessons()
        );
        emptyLessonsText.setVisibility(View.GONE);
        parentLessonsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmpty(String message) {
        adapter.updateLessons(null, false, false, null);
        emptyLessonsText.setText(message);
        emptyLessonsText.setVisibility(View.VISIBLE);
        parentLessonsRecyclerView.setVisibility(View.GONE);
    }

    private List<ParentLessonItem> getVisibleLessons(List<ParentLessonItem> lessons, boolean showAll) {
        if (lessons == null || showAll || lessons.size() <= 3) {
            return lessons;
        }

        return new ArrayList<>(lessons.subList(0, 3));
    }

    private void toggleShowAllLessons() {
        if (isFutureMode) {
            showAllFutureLessons = !showAllFutureLessons;
            showFutureLessons();
        } else {
            showAllMissedLessons = !showAllMissedLessons;
            showMissedLessons();
        }
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
        TextView lessonChildText = view.findViewById(R.id.lessonChildText);
        LinearLayout lessonBadgesRow = view.findViewById(R.id.lessonBadgesRow);
        TextView lessonActionsHintText = view.findViewById(R.id.lessonActionsHintText);

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
        lessonTopicText.setText(safe(lesson.getTopic()));
        if (hasRealValue(lesson.getTeacherName())) {
            lessonTeacherText.setVisibility(View.VISIBLE);
            lessonTeacherText.setText("Преподаватель: " + lesson.getTeacherName());
        } else {
            lessonTeacherText.setVisibility(View.GONE);
        }
        if (showChildNameInLessonCards && !isEmpty(lesson.getChildNames())
                && !"не указано".equalsIgnoreCase(lesson.getChildNames())) {
            lessonChildText.setVisibility(View.VISIBLE);
            lessonChildText.setText(lesson.getChildNames());
            lessonChildText.setBackground(createBadgeBackground(getChildBadgeColor(lesson.getChildId())));
        } else {
            lessonChildText.setVisibility(View.GONE);
        }

        boolean missedLesson = isMissedLesson(lesson);
        boolean canCancelLesson = shouldShowCancelAction(lesson);
        boolean cancelledByParent = isCancelledByParent(lesson);
        boolean rescheduledLesson = isRescheduledLesson(lesson);

        boolean hasStatusBadge = true;
        if (cancelledByParent) {
            lessonStatusText.setVisibility(View.VISIBLE);
            lessonStatusText.setText("Отменено");
            lessonStatusText.setBackground(createBadgeBackground(Color.parseColor("#F05A5A")));
        } else if ("RESCHEDULED_OUT".equalsIgnoreCase(lesson.getChildStatus())) {
            lessonStatusText.setVisibility(View.VISIBLE);
            lessonStatusText.setText("Перенесено");
            lessonStatusText.setBackground(createBadgeBackground(Color.parseColor("#FF6B00")));
        } else if ("RESCHEDULED_IN".equalsIgnoreCase(lesson.getChildStatus())) {
            lessonStatusText.setVisibility(View.VISIBLE);
            lessonStatusText.setText("Перенесено сюда");
            lessonStatusText.setBackground(createBadgeBackground(Color.parseColor("#A56BE8")));
        } else if (missedLesson) {
            lessonStatusText.setVisibility(View.VISIBLE);
            lessonStatusText.setText("Пропуск");
            lessonStatusText.setBackground(createBadgeBackground(Color.parseColor("#6CBF4A")));
        } else {
            lessonStatusText.setVisibility(View.GONE);
            hasStatusBadge = false;
        }

        lessonBadgesRow.setVisibility(
                hasStatusBadge || lessonChildText.getVisibility() == View.VISIBLE
                        ? View.VISIBLE
                        : View.GONE
        );
        updateChildBadgeMargin(lessonChildText, hasStatusBadge);

        if (cancelledByParent) {
            lessonActionsHintText.setVisibility(View.GONE);
            moveLessonAction.setVisibility(View.VISIBLE);
            cancelLessonAction.setVisibility(View.GONE);
        } else if (rescheduledLesson) {
            lessonActionsHintText.setVisibility(View.VISIBLE);
            moveLessonAction.setVisibility(View.GONE);
            cancelLessonAction.setVisibility(View.GONE);
        } else {
            lessonActionsHintText.setVisibility(View.GONE);
            moveLessonAction.setVisibility(View.VISIBLE);
            if (canCancelLesson) {
                cancelLessonAction.setVisibility(View.VISIBLE);
            } else {
                cancelLessonAction.setVisibility(View.GONE);
            }

            cancelLessonAction.setOnClickListener(v -> showCancelConfirmation(dialog, lesson));
        }

        moveLessonAction.setOnClickListener(v -> {
            if (isEmpty(lesson.getChildId()) || isEmpty(lesson.getLessonId())) {
                Log.e(TAG, "reschedule open blocked lessonId=" + lesson.getLessonId()
                        + ", childId=" + lesson.getChildId());
                Toast.makeText(requireContext(), "Не удалось открыть перенос занятия", Toast.LENGTH_SHORT).show();
                return;
            }

            List<ParentLessonItem> targetLessons = getRescheduleTargetLessons(lesson);
            dialog.dismiss();
            openRescheduleFragment(lesson, targetLessons);
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showCancelConfirmation(BottomSheetDialog dialog, ParentLessonItem lesson) {
        new AlertDialog.Builder(requireContext())
                .setMessage("Вы уверены, что хотите отменить занятие?")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Подтвердить", (confirmDialog, which) -> {
                    dialog.dismiss();
                    cancelLesson(lesson);
                })
                .show();
    }

    private void openRescheduleFragment(ParentLessonItem sourceLesson,
                                        List<ParentLessonItem> targetLessons) {
        ParentLessonRescheduleFragment fragment = ParentLessonRescheduleFragment.newInstance(
                sourceLesson,
                new ArrayList<>(targetLessons)
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.parentFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void cancelLesson(ParentLessonItem lesson) {
        if (lesson.getChildId() == null || lesson.getChildId().isEmpty()
                || lesson.getLessonId() == null || lesson.getLessonId().isEmpty()) {
            Toast.makeText(requireContext(), "Не удалось определить занятие для отмены", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!shouldShowCancelAction(lesson)) {
            Toast.makeText(requireContext(), "Это занятие нельзя отменить", Toast.LENGTH_SHORT).show();
            return;
        }

        ParentLessonActionApi actionApi = ApiClient.getClient().create(ParentLessonActionApi.class);
        CancelLessonRequest request = new CancelLessonRequest("Отменено родителем");

        String currentAuthHeader = getCurrentAuthHeader();
        if (currentAuthHeader.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        actionApi.cancelLesson(currentAuthHeader, lesson.getChildId(), lesson.getLessonId(), request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!isAdded()) {
                            return;
                        }

                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Занятие отменено", Toast.LENGTH_SHORT).show();
                            lessonsStorage.clear();
                            refreshParentLessonsFromBackend();
                        } else if (response.code() == 409) {
                            Log.e(TAG, "cancel failed code=" + response.code()
                                    + ", body=" + getErrorBodyString(response));
                            Toast.makeText(requireContext(), "Это занятие нельзя отменить", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "cancel failed code=" + response.code()
                                    + ", body=" + getErrorBodyString(response));
                            Toast.makeText(requireContext(), "Не удалось отменить занятие", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(requireContext(), "Ошибка отмены занятия", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean shouldShowCancelAction(ParentLessonItem lesson) {
        boolean missed = isMissedLesson(lesson);
        boolean future = isFutureLesson(lesson);
        boolean childStatusEmpty = isEmpty(lesson.getChildStatus());
        return isFutureMode && !missed && future && childStatusEmpty;
    }

    private boolean shouldShowInFutureList(String childStatus) {
        return isEmpty(childStatus) || "RESCHEDULED_IN".equalsIgnoreCase(childStatus);
    }

    private boolean isCancelledByParent(ParentLessonItem lesson) {
        return lesson != null && ("CANCELLED_BY_PARENT".equalsIgnoreCase(lesson.getChildStatus())
                || "CANCELLED_BY_PARENT".equalsIgnoreCase(lesson.getStatus()));
    }

    private boolean isRescheduledLesson(ParentLessonItem lesson) {
        return lesson != null && ("RESCHEDULED_OUT".equalsIgnoreCase(lesson.getChildStatus())
                || "RESCHEDULED_IN".equalsIgnoreCase(lesson.getChildStatus()));
    }

    private List<ParentLessonItem> getRescheduleTargetLessons(ParentLessonItem sourceLesson) {
        List<ParentLessonItem> targetLessons = new ArrayList<>();

        if (sourceLesson == null) {
            return targetLessons;
        }

        for (ParentLessonItem lesson : loadedLessons) {
            if (lesson == null) {
                continue;
            }

            if (isEmpty(lesson.getLessonId())) {
                continue;
            } else if (safeRaw(sourceLesson.getLessonId()).equals(safeRaw(lesson.getLessonId()))) {
                continue;
            } else if (!safeRaw(sourceLesson.getChildId()).equals(safeRaw(lesson.getChildId()))) {
                continue;
            } else if (!isSameGroupForReschedule(sourceLesson, lesson)) {
                continue;
            } else if (!isSameCourseForReschedule(sourceLesson, lesson)) {
                continue;
            } else if (isSchoolCancelled(lesson)) {
                continue;
            } else if (!isFutureLesson(lesson)) {
                continue;
            } else if (!isEmpty(lesson.getChildStatus())) {
                continue;
            }

            targetLessons.add(lesson);
        }

        Collections.sort(targetLessons, Comparator.comparingLong(ParentLessonItem::getDateMillis));
        return targetLessons;
    }

    private boolean isSameGroupForReschedule(ParentLessonItem sourceLesson, ParentLessonItem candidateLesson) {
        if (!isEmpty(sourceLesson.getGroupId()) && !isEmpty(candidateLesson.getGroupId())) {
            return safeRaw(sourceLesson.getGroupId()).equals(safeRaw(candidateLesson.getGroupId()));
        }

        return true;
    }

    private boolean isSameCourseForReschedule(ParentLessonItem sourceLesson, ParentLessonItem candidateLesson) {
        return safeRaw(sourceLesson.getCourseName()).equals(safeRaw(candidateLesson.getCourseName()));
    }

    private boolean isFutureLesson(ParentLessonItem lesson) {
        return lesson != null && lesson.getDateMillis() > System.currentTimeMillis();
    }

    private boolean isMissedLesson(ParentLessonItem lesson) {
        return isAbsentLesson(lesson);
    }

    private boolean isAbsentLesson(ParentLessonItem lesson) {
        return lesson != null && ("ABSENT".equalsIgnoreCase(lesson.getStatus())
                || "ABSENT".equalsIgnoreCase(lesson.getChildStatus()));
    }

    private boolean isSchoolCancelled(ParentLessonItem lesson) {
        return lesson != null && "CANCELLED".equalsIgnoreCase(lesson.getStatus());
    }

    private String getErrorBodyString(Response<?> response) {
        if (response.errorBody() == null) {
            return "";
        }

        try {
            return response.errorBody().string();
        } catch (IOException e) {
            return "error body read failed: " + e.getMessage();
        }
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

    private void updateChildBadgeMargin(TextView childText, boolean hasStatusBadge) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) childText.getLayoutParams();
        params.setMarginStart(hasStatusBadge ? dp(8) : 0);
        childText.setLayoutParams(params);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String getTeacherName(LessonDto lesson, GroupDto group) {
        if (lesson.getTeacherName() != null && !lesson.getTeacherName().isEmpty()) {
            return lesson.getTeacherName();
        }

        if (group != null && group.getTeacherName() != null && !group.getTeacherName().isEmpty()) {
            return group.getTeacherName();
        }

        return "не указано";
    }

    private String safeRaw(String value) {
        return value == null ? "" : value;
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private String getCurrentAuthHeader() {
        if (authHeader != null && !authHeader.isEmpty()) {
            return authHeader;
        }

        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

        if (token == null || token.isEmpty()) {
            return "";
        }

        authHeader = "Bearer " + token;
        return authHeader;
    }
}
