package com.github.nsu_upprpo.school_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.adapter.ScheduleDayAdapter;
import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.GroupApi;
import com.github.nsu_upprpo.school_app.model.GroupDto;
import com.github.nsu_upprpo.school_app.model.ScheduleDay;
import com.github.nsu_upprpo.school_app.model.ScheduleItem;
import com.github.nsu_upprpo.school_app.storage.ScheduleStorage;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;
import com.github.nsu_upprpo.school_app.api.ScheduleApi;
import com.github.nsu_upprpo.school_app.model.LessonDto;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherScheduleFragment extends Fragment {

    private final List<ScheduleDay> allDays = new ArrayList<>();
    private ScheduleDayAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyTodayLayout;
    private TextView todayTab;
    private TextView weekTab;
    private boolean isTodayMode = true;
    private ScheduleStorage scheduleStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_teacher_schedule, container, false);

        recyclerView = view.findViewById(R.id.teacherScheduleRecyclerView);
        emptyTodayLayout = view.findViewById(R.id.teacherTodayEmptyLayout);
        todayTab = view.findViewById(R.id.teacherTodayTab);
        weekTab = view.findViewById(R.id.teacherWeekTab);

        scheduleStorage = new ScheduleStorage(requireContext());
        isTodayMode = scheduleStorage.isTeacherTodayMode();

        adapter = new ScheduleDayAdapter(new ArrayList<>(), item -> {
            if (item.getLessonId() == null || item.getLessonId().isEmpty()) {
                Toast.makeText(requireContext(), "Не найден id занятия", Toast.LENGTH_SHORT).show();
                return;
            }

            TeacherAttendanceFragment fragment = TeacherAttendanceFragment.newInstance(
                    item.getLessonId(),
                    item.getGroupId(),
                    item.getTitle(),
                    item.getSubtitle() + " • " + item.getTime()
            );

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.teacherFragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        todayTab.setOnClickListener(v -> {
            isTodayMode = true;
            scheduleStorage.setTeacherTodayMode(true);
            updateUiByMode();
        });

        weekTab.setOnClickListener(v -> {
            isTodayMode = false;
            scheduleStorage.setTeacherTodayMode(false);
            updateUiByMode();
        });

        if (tryLoadFromCache()) {
            updateUiByMode();
        } else {
            allDays.clear();
            allDays.addAll(createEmptyWeek());
            updateUiByMode();
        }

        loadTeacherGroups();

        return view;
    }

    private void saveToCache() {
        Gson gson = new Gson();
        String json = gson.toJson(allDays);
        scheduleStorage.saveTeacherScheduleJson(json);
    }

    private boolean tryLoadFromCache() {
        if (!scheduleStorage.hasTeacherSchedule()) {
            return false;
        }

        String json = scheduleStorage.getTeacherScheduleJson();

        if (json == null || json.isEmpty()) {
            return false;
        }

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ScheduleDay>>() {}.getType();
            List<ScheduleDay> cachedDays = gson.fromJson(json, type);

            if (cachedDays == null || cachedDays.isEmpty()) {
                return false;
            }

            allDays.clear();
            allDays.addAll(cachedDays);
            return true;

        } catch (Exception e) {
            scheduleStorage.clear();
            return false;
        }
    }

    private void updateUiByMode() {
        if (isTodayMode) {
            setTodayTabActive();
            List<ScheduleDay> todayDays = getTodayOnly();
            adapter.updateDays(todayDays);
            updateTodayEmptyState(todayDays);
        } else {
            setWeekTabActive();
            recyclerView.setVisibility(View.VISIBLE);
            emptyTodayLayout.setVisibility(View.GONE);
            adapter.updateDays(new ArrayList<>(allDays));
        }
    }

    private void updateTodayEmptyState(List<ScheduleDay> todayDays) {
        boolean hasLessons = false;

        for (ScheduleDay day : todayDays) {
            if (day.getItems() != null && !day.getItems().isEmpty()) {
                hasLessons = true;
                break;
            }
        }

        recyclerView.setVisibility(hasLessons ? View.VISIBLE : View.GONE);
        emptyTodayLayout.setVisibility(hasLessons ? View.GONE : View.VISIBLE);
    }

    private void loadTeacherGroups() {
        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;

        GroupApi groupApi = ApiClient.getClient().create(GroupApi.class);

        groupApi.getTeacherGroups(authHeader).enqueue(new Callback<List<GroupDto>>() {
            @Override
            public void onResponse(Call<List<GroupDto>> call, Response<List<GroupDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    loadLessonsForGroups(authHeader, response.body());
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить группы", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GroupDto>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLessonsForGroups(String authHeader, List<GroupDto> groups) {
        Map<String, List<ScheduleItem>> dayMap = createWeekMap();

        if (groups == null || groups.isEmpty()) {
            allDays.clear();
            allDays.addAll(mapDayMapToScheduleDays(dayMap));
            updateUiByMode();
            return;
        }

        ScheduleApi scheduleApi = ApiClient.getClient().create(ScheduleApi.class);

        final int[] pending = {groups.size()};
        final boolean[] hasPartialError = {false};

        for (GroupDto group : groups) {
            scheduleApi.getGroupSchedule(authHeader, group.getGroupId())
                    .enqueue(new Callback<List<LessonDto>>() {
                        @Override
                        public void onResponse(Call<List<LessonDto>> call, Response<List<LessonDto>> response) {
                            if (!isAdded()) return;

                            if (response.isSuccessful() && response.body() != null) {
                                addLessonsToDayMap(dayMap, group, response.body());
                            } else {
                                hasPartialError[0] = true;
                            }

                            pending[0]--;

                            if (pending[0] == 0) {
                                finishScheduleLoading(dayMap, !hasPartialError[0]);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<LessonDto>> call, Throwable t) {
                            if (!isAdded()) return;

                            hasPartialError[0] = true;
                            pending[0]--;

                            if (pending[0] == 0) {
                                finishScheduleLoading(dayMap, false);
                            }
                        }
                    });
        }
    }

    private void addLessonsToDayMap(Map<String, List<ScheduleItem>> dayMap,
                                    GroupDto group,
                                    List<LessonDto> lessons) {
        for (LessonDto lesson : lessons) {
            String dayName = getRussianDayNameFromDateTime(lesson.getStartTime());

            List<ScheduleItem> items = dayMap.get(dayName);

            if (items == null) {
                continue;
            }

            String title = safe(group.getCourseName());
            String subtitle = safe(group.getBranchName());
            String time = formatTime(lesson.getStartTime()) + "-" + formatTime(lesson.getEndTime());
            int color = R.color.course_orange;

            ScheduleItem item = new ScheduleItem(
                    lesson.getId(),
                    lesson.getGroupId(),
                    title,
                    subtitle,
                    time,
                    color
            );

            items.add(item);
        }
    }

    private String getRussianDayNameFromDateTime(String dateTime) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = input.parse(dateTime);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int day = calendar.get(Calendar.DAY_OF_WEEK);

            switch (day) {
                case Calendar.MONDAY:
                    return "Понедельник";
                case Calendar.TUESDAY:
                    return "Вторник";
                case Calendar.WEDNESDAY:
                    return "Среда";
                case Calendar.THURSDAY:
                    return "Четверг";
                case Calendar.FRIDAY:
                    return "Пятница";
                case Calendar.SATURDAY:
                    return "Суббота";
                case Calendar.SUNDAY:
                    return "Воскресенье";
                default:
                    return "Понедельник";
            }
        } catch (ParseException | NullPointerException e) {
            return "Понедельник";
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

    private void finishScheduleLoading(Map<String, List<ScheduleItem>> dayMap, boolean canSaveToCache) {
        for (List<ScheduleItem> items : dayMap.values()) {
            items.sort((a, b) -> Integer.compare(
                    parseStartMinutes(a.getTime()),
                    parseStartMinutes(b.getTime())
            ));
        }

        allDays.clear();
        allDays.addAll(mapDayMapToScheduleDays(dayMap));

        if (canSaveToCache) {
            saveToCache();
        }

        updateUiByMode();
    }

    private List<ScheduleDay> mapDayMapToScheduleDays(Map<String, List<ScheduleItem>> dayMap) {
        List<ScheduleDay> result = new ArrayList<>();

        for (Map.Entry<String, List<ScheduleItem>> entry : dayMap.entrySet()) {
            result.add(new ScheduleDay(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    private Map<String, List<ScheduleItem>> createWeekMap() {
        Map<String, List<ScheduleItem>> dayMap = new LinkedHashMap<>();

        dayMap.put("Понедельник", new ArrayList<>());
        dayMap.put("Вторник", new ArrayList<>());
        dayMap.put("Среда", new ArrayList<>());
        dayMap.put("Четверг", new ArrayList<>());
        dayMap.put("Пятница", new ArrayList<>());
        dayMap.put("Суббота", new ArrayList<>());
        dayMap.put("Воскресенье", new ArrayList<>());

        return dayMap;
    }

    private List<ScheduleDay> createEmptyWeek() {
        List<ScheduleDay> days = new ArrayList<>();

        days.add(new ScheduleDay("Понедельник", new ArrayList<>()));
        days.add(new ScheduleDay("Вторник", new ArrayList<>()));
        days.add(new ScheduleDay("Среда", new ArrayList<>()));
        days.add(new ScheduleDay("Четверг", new ArrayList<>()));
        days.add(new ScheduleDay("Пятница", new ArrayList<>()));
        days.add(new ScheduleDay("Суббота", new ArrayList<>()));
        days.add(new ScheduleDay("Воскресенье", new ArrayList<>()));

        return days;
    }

    private List<ScheduleDay> getTodayOnly() {
        String todayName = getTodayRussianName();
        List<ScheduleDay> result = new ArrayList<>();

        for (ScheduleDay day : allDays) {
            if (day.getDayTitle().equalsIgnoreCase(todayName)) {
                result.add(day);
                break;
            }
        }

        if (result.isEmpty()) {
            result.add(new ScheduleDay(todayName, new ArrayList<>()));
        }

        return result;
    }

    private int parseStartMinutes(String time) {
        if (time == null || !time.contains("-")) {
            return Integer.MAX_VALUE;
        }

        String start = time.split("-")[0].trim();
        String[] parts = start.split(":");

        if (parts.length != 2) {
            return Integer.MAX_VALUE;
        }

        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private String getTodayRussianName() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.MONDAY:
                return "Понедельник";
            case Calendar.TUESDAY:
                return "Вторник";
            case Calendar.WEDNESDAY:
                return "Среда";
            case Calendar.THURSDAY:
                return "Четверг";
            case Calendar.FRIDAY:
                return "Пятница";
            case Calendar.SATURDAY:
                return "Суббота";
            case Calendar.SUNDAY:
                return "Воскресенье";
            default:
                return "Понедельник";
        }
    }

    private void setTodayTabActive() {
        todayTab.setBackgroundResource(R.drawable.bg_button_orange);
        todayTab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        weekTab.setBackgroundResource(R.drawable.bg_tab_inactive);
        weekTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
    }

    private void setWeekTabActive() {
        weekTab.setBackgroundResource(R.drawable.bg_button_orange);
        weekTab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        todayTab.setBackgroundResource(R.drawable.bg_tab_inactive);
        todayTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
