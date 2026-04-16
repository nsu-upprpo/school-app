package com.example.schoolapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolapp.R;
import com.example.schoolapp.adapter.ScheduleDayAdapter;
import com.example.schoolapp.model.ScheduleDay;
import com.example.schoolapp.model.ScheduleItem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParentScheduleFragment extends Fragment {

    private final List<ScheduleDay> allDays = new ArrayList<>();
    private ScheduleDayAdapter adapter;

    private TextView todayTab;
    private TextView weekTab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_schedule, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.parentScheduleRecyclerView);
        todayTab = view.findViewById(R.id.parentTodayTab);
        weekTab = view.findViewById(R.id.parentWeekTab);

        allDays.clear();
        allDays.addAll(createMockDays());

        adapter = new ScheduleDayAdapter(allDays);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        weekTab.setOnClickListener(v -> showWeekMode());
        todayTab.setOnClickListener(v -> showTodayMode());

        showTodayMode();

        return view;
    }

    private void showWeekMode() {
        setWeekTabActive();
        adapter.updateDays(allDays);
    }

    private void showTodayMode() {
        setTodayTabActive();
        adapter.updateDays(getTodayOnly());
    }

    private List<ScheduleDay> getTodayOnly() {
        String todayName = getTodayRussianName();

        for (ScheduleDay day : allDays) {
            if (day.getDayTitle().startsWith(todayName)) {
                return Arrays.asList(day);
            }
        }

        return new ArrayList<>();
    }

    private String getTodayRussianName() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();

        switch (dayOfWeek) {
            case MONDAY:
                return "Понедельник";
            case TUESDAY:
                return "Вторник";
            case WEDNESDAY:
                return "Среда";
            case THURSDAY:
                return "Четверг";
            case FRIDAY:
                return "Пятница";
            case SATURDAY:
                return "Суббота";
            case SUNDAY:
                return "Воскресенье";
            default:
                return "Понедельник";
        }
    }

    private LocalDate getStartOfCurrentWeek() {
        LocalDate today = LocalDate.now();
        int diff = today.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        return today.minusDays(diff);
    }

    private String formatRussianDayWithDate(LocalDate date) {
        String dayName;
        switch (date.getDayOfWeek()) {
            case MONDAY:
                dayName = "Понедельник";
                break;
            case TUESDAY:
                dayName = "Вторник";
                break;
            case WEDNESDAY:
                dayName = "Среда";
                break;
            case THURSDAY:
                dayName = "Четверг";
                break;
            case FRIDAY:
                dayName = "Пятница";
                break;
            case SATURDAY:
                dayName = "Суббота";
                break;
            case SUNDAY:
                dayName = "Воскресенье";
                break;
            default:
                dayName = "";
        }

        String monthName;
        switch (date.getMonth()) {
            case JANUARY:
                monthName = "января";
                break;
            case FEBRUARY:
                monthName = "февраля";
                break;
            case MARCH:
                monthName = "марта";
                break;
            case APRIL:
                monthName = "апреля";
                break;
            case MAY:
                monthName = "мая";
                break;
            case JUNE:
                monthName = "июня";
                break;
            case JULY:
                monthName = "июля";
                break;
            case AUGUST:
                monthName = "августа";
                break;
            case SEPTEMBER:
                monthName = "сентября";
                break;
            case OCTOBER:
                monthName = "октября";
                break;
            case NOVEMBER:
                monthName = "ноября";
                break;
            case DECEMBER:
                monthName = "декабря";
                break;
            default:
                monthName = "";
        }

        return dayName + ", " + date.getDayOfMonth() + " " + monthName;
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

    private List<ScheduleDay> createMockDays() {
        List<ScheduleDay> days = new ArrayList<>();
        LocalDate monday = getStartOfCurrentWeek();

        days.add(new ScheduleDay(formatRussianDayWithDate(monday), Arrays.asList(
                new ScheduleItem("Композиция", "Группа 1", "16:20–19:00", R.color.course_green),
                new ScheduleItem("Рисунок", "Группа 2", "17:00–19:40", R.color.course_blue)
        )));

        days.add(new ScheduleDay(formatRussianDayWithDate(monday.plusDays(1)), Arrays.asList(
                new ScheduleItem("Живопись", "Группа 3", "15:00–17:30", R.color.course_yellow)
        )));

        days.add(new ScheduleDay(formatRussianDayWithDate(monday.plusDays(2)), Arrays.asList(
                new ScheduleItem("Проект", "Группа 1", "16:20–19:00", R.color.course_orange)
        )));

        days.add(new ScheduleDay(formatRussianDayWithDate(monday.plusDays(3)), new ArrayList<>()));

        days.add(new ScheduleDay(formatRussianDayWithDate(monday.plusDays(4)), Arrays.asList(
                new ScheduleItem("Макетирование", "Группа 4", "14:20–17:00", R.color.course_purple),
                new ScheduleItem("Цветоведение", "Группа 2", "17:30–19:30", R.color.course_green)
        )));

        days.add(new ScheduleDay(formatRussianDayWithDate(monday.plusDays(5)), Arrays.asList(
                new ScheduleItem("Дизайн", "Группа 5", "10:30–13:10", R.color.course_blue)
        )));

        days.add(new ScheduleDay(formatRussianDayWithDate(monday.plusDays(6)), new ArrayList<>()));

        return days;
    }
}