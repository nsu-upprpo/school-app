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
import com.google.gson.Gson;
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
    private TextView todayTab;
    private TextView weekTab;
    private boolean isTodayMode = true;
    private ScheduleStorage scheduleStorage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_teacher_schedule, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.teacherScheduleRecyclerView);
        todayTab = view.findViewById(R.id.teacherTodayTab);
        weekTab = view.findViewById(R.id.teacherWeekTab);

        scheduleStorage = new ScheduleStorage(requireContext());
        isTodayMode = scheduleStorage.isTeacherTodayMode();

        adapter = new ScheduleDayAdapter(new ArrayList<>());
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
            loadTeacherGroups();
        }

        return view;
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

            if (cachedDays == null) {
                return false;
            }

            allDays.clear();
            allDays.addAll(cachedDays);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private void saveToCache() {
        Gson gson = new Gson();
        String json = gson.toJson(allDays);
        scheduleStorage.saveTeacherScheduleJson(json);
    }

    private void updateUiByMode() {
        if (isTodayMode) {
            setTodayTabActive();
            adapter.updateDays(getTodayOnly());
        } else {
            setWeekTabActive();
            adapter.updateDays(new ArrayList<>(allDays));
        }
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
                    allDays.clear();
                    allDays.addAll(mapGroupsToScheduleDays(response.body()));

                    saveToCache();
                    updateUiByMode();
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить расписание", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GroupDto>> call, Throwable t) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<ScheduleDay> mapGroupsToScheduleDays(List<GroupDto> groups) {
        Map<String, List<ScheduleItem>> dayMap = createWeekMap();

        for (GroupDto group : groups) {
            String title = safe(group.getCourseName());
            String subtitle = safe(group.getBranchName()) + " • " + safe(group.getTeacherName());
            String scheduleDescription = safe(group.getScheduleDescription());
            String time = extractTime(scheduleDescription);
            int color = pickColorByCourseId(group.getCourseId());

            List<String> days = extractRussianDays(scheduleDescription);

            for (String day : days) {
                List<ScheduleItem> items = dayMap.get(day);

                if (items != null) {
                    items.add(new ScheduleItem(title, subtitle, time, color));
                }
            }
        }

        for (List<ScheduleItem> items : dayMap.values()) {
            items.sort((a, b) -> Integer.compare(
                    parseStartMinutes(a.getTime()),
                    parseStartMinutes(b.getTime())
            ));
        }

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

    private List<String> extractRussianDays(String scheduleDescription) {
        String s = scheduleDescription.toUpperCase(Locale.ROOT);
        List<String> days = new ArrayList<>();

        if (s.contains("ПН")) days.add("Понедельник");
        if (s.contains("ВТ")) days.add("Вторник");
        if (s.contains("СР")) days.add("Среда");
        if (s.contains("ЧТ")) days.add("Четверг");
        if (s.contains("ПТ")) days.add("Пятница");
        if (s.contains("СБ")) days.add("Суббота");
        if (s.contains("ВС")) days.add("Воскресенье");

        return days;
    }

    private String extractTime(String scheduleDescription) {
        if (scheduleDescription == null) return "";

        String[] parts = scheduleDescription.trim().split("\\s+");

        if (parts.length == 0) return "";

        String lastPart = parts[parts.length - 1];

        if (lastPart.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
            return lastPart;
        }

        return scheduleDescription;
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

    private int pickColorByCourseId(String courseId) {
        int[] colors = {
                R.color.course_green,
                R.color.course_blue,
                R.color.course_yellow,
                R.color.course_orange,
                R.color.course_purple
        };

        if (courseId == null || courseId.isEmpty()) {
            return R.color.course_orange;
        }

        int index = Math.abs(courseId.hashCode()) % colors.length;
        return colors[index];
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