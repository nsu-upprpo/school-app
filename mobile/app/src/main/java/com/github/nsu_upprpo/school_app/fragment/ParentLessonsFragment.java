package com.github.nsu_upprpo.school_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.adapter.ParentLessonAdapter;
import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.GroupApi;
import com.github.nsu_upprpo.school_app.model.GroupDto;
import com.github.nsu_upprpo.school_app.model.ParentLessonItem;
import com.github.nsu_upprpo.school_app.storage.ParentLessonsStorage;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.Typeface;
import androidx.core.content.ContextCompat;

public class ParentLessonsFragment extends Fragment {

    private RecyclerView parentLessonsRecyclerView;
    private TextView emptyLessonsText;
    private TextView parentFutureTab;
    private TextView parentMissedTab;
    private ParentLessonAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_lessons, container, false);

        parentFutureTab = view.findViewById(R.id.parentFutureTab);
        parentMissedTab = view.findViewById(R.id.parentMissedTab);
        parentLessonsRecyclerView = view.findViewById(R.id.parentLessonsRecyclerView);
        emptyLessonsText = view.findViewById(R.id.emptyLessonsText);

        adapter = new ParentLessonAdapter();
        parentLessonsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        parentLessonsRecyclerView.setAdapter(adapter);

        parentFutureTab.setOnClickListener(v -> {
            parentFutureTab.setBackgroundResource(R.drawable.bg_button_orange);
            parentFutureTab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            parentFutureTab.setTypeface(null, Typeface.BOLD);

            parentMissedTab.setBackgroundResource(R.drawable.bg_tab_inactive);
            parentMissedTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
            parentMissedTab.setTypeface(null, Typeface.NORMAL);

            loadLessons();
        });

        parentMissedTab.setOnClickListener(v -> {
            parentMissedTab.setBackgroundResource(R.drawable.bg_button_orange);
            parentMissedTab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            parentMissedTab.setTypeface(null, Typeface.BOLD);

            parentFutureTab.setBackgroundResource(R.drawable.bg_tab_inactive);
            parentFutureTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
            parentFutureTab.setTypeface(null, Typeface.NORMAL);

            adapter.updateLessons(null);
            emptyLessonsText.setText("Пропуски пока не реализованы");
            emptyLessonsText.setVisibility(View.VISIBLE);
            parentLessonsRecyclerView.setVisibility(View.GONE);
        });

        loadLessons();

        return view;
    }

    private void loadLessons() {
        ParentLessonsStorage storage = new ParentLessonsStorage(requireContext());

        if (storage.hasLessons()) {
            showLessons(storage.getLessons());
            return;
        }

        loadLessonsFromBackend(storage);
    }

    private void loadLessonsFromBackend(ParentLessonsStorage storage) {
        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;

        GroupApi groupApi = ApiClient.getClient().create(GroupApi.class);

        groupApi.getParentGroups(authHeader).enqueue(new Callback<List<GroupDto>>() {
            @Override
            public void onResponse(Call<List<GroupDto>> call, Response<List<GroupDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<ParentLessonItem> lessons = buildNearestLessons(response.body());
                    storage.saveLessons(lessons);
                    showLessons(lessons);
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить занятия", Toast.LENGTH_SHORT).show();
                    showLessons(null);
                }
            }

            @Override
            public void onFailure(Call<List<GroupDto>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showLessons(null);
            }
        });
    }

    private void showLessons(List<ParentLessonItem> lessons) {
        adapter.updateLessons(lessons);

        if (lessons == null || lessons.isEmpty()) {
            emptyLessonsText.setVisibility(View.VISIBLE);
            parentLessonsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyLessonsText.setVisibility(View.GONE);
            parentLessonsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private List<ParentLessonItem> buildNearestLessons(List<GroupDto> groups) {
        List<ParentLessonItem> result = new ArrayList<>();

        for (GroupDto group : groups) {
            result.addAll(buildLessonsForGroup(group));
        }

        result.sort(Comparator.comparingLong(ParentLessonItem::getDateMillis));

        if (result.size() > 3) {
            return new ArrayList<>(result.subList(0, 3));
        }

        return result;
    }

    private List<ParentLessonItem> buildLessonsForGroup(GroupDto group) {
        List<ParentLessonItem> lessons = new ArrayList<>();

        String schedule = group.getScheduleDescription();
        if (schedule == null || schedule.isEmpty()) {
            return lessons;
        }

        TimeRange timeRange = parseTimeRange(schedule);
        if (timeRange == null) {
            return lessons;
        }

        List<Integer> days = parseDays(schedule);

        for (Integer dayOfWeek : days) {
            long dateMillis = getNextDateMillis(dayOfWeek, timeRange.startTime);

            lessons.add(new ParentLessonItem(
                    safe(group.getCourseName()),
                    safe(group.getTeacherName()),
                    timeRange.startTime,
                    timeRange.endTime,
                    dateMillis
            ));
        }

        return lessons;
    }

    private TimeRange parseTimeRange(String schedule) {
        Pattern pattern = Pattern.compile("(\\d{1,2}:\\d{2})\\s*-\\s*(\\d{1,2}:\\d{2})");
        Matcher matcher = pattern.matcher(schedule);

        if (!matcher.find()) {
            return null;
        }

        return new TimeRange(matcher.group(1), matcher.group(2));
    }

    private List<Integer> parseDays(String schedule) {
        List<Integer> days = new ArrayList<>();

        String upper = schedule.toUpperCase(Locale.ROOT);

        if (upper.contains("ПН")) days.add(Calendar.MONDAY);
        if (upper.contains("ВТ")) days.add(Calendar.TUESDAY);
        if (upper.contains("СР")) days.add(Calendar.WEDNESDAY);
        if (upper.contains("ЧТ")) days.add(Calendar.THURSDAY);
        if (upper.contains("ПТ")) days.add(Calendar.FRIDAY);
        if (upper.contains("СБ")) days.add(Calendar.SATURDAY);
        if (upper.contains("ВС")) days.add(Calendar.SUNDAY);

        return days;
    }

    private long getNextDateMillis(int targetDayOfWeek, String startTime) {
        Calendar now = Calendar.getInstance();
        Calendar result = Calendar.getInstance();

        int currentDay = now.get(Calendar.DAY_OF_WEEK);
        int daysToAdd = targetDayOfWeek - currentDay;

        if (daysToAdd < 0) {
            daysToAdd += 7;
        }

        result.add(Calendar.DAY_OF_MONTH, daysToAdd);

        String[] timeParts = startTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        result.set(Calendar.HOUR_OF_DAY, hour);
        result.set(Calendar.MINUTE, minute);
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.MILLISECOND, 0);

        if (result.before(now)) {
            result.add(Calendar.DAY_OF_MONTH, 7);
        }

        return result.getTimeInMillis();
    }

    private String safe(String value) {
        return value == null || value.isEmpty() ? "не указано" : value;
    }

    private static class TimeRange {
        String startTime;
        String endTime;

        TimeRange(String startTime, String endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}