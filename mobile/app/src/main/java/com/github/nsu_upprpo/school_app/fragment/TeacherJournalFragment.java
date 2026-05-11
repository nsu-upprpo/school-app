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
import com.github.nsu_upprpo.school_app.adapter.TeacherGroupAdapter;
import com.github.nsu_upprpo.school_app.adapter.TeacherLessonAdapter;
import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.GroupApi;
import com.github.nsu_upprpo.school_app.api.ScheduleApi;
import com.github.nsu_upprpo.school_app.model.GroupDto;
import com.github.nsu_upprpo.school_app.model.LessonDto;
import com.github.nsu_upprpo.school_app.storage.TeacherJournalStorage;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherJournalFragment extends Fragment {

    private TextView backText;
    private TextView titleText;
    private TextView subtitleText;
    private TextView emptyText;
    private RecyclerView recyclerView;

    private TeacherGroupAdapter groupAdapter;
    private TeacherLessonAdapter lessonAdapter;

    private TeacherJournalStorage journalStorage;
    private String authHeader;

    private GroupDto selectedGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_teacher_journal, container, false);

        backText = view.findViewById(R.id.teacherJournalBackText);
        titleText = view.findViewById(R.id.teacherJournalTitle);
        subtitleText = view.findViewById(R.id.teacherJournalSubtitle);
        emptyText = view.findViewById(R.id.teacherJournalEmptyText);
        recyclerView = view.findViewById(R.id.teacherJournalRecyclerView);

        journalStorage = new TeacherJournalStorage(requireContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        groupAdapter = new TeacherGroupAdapter(this::openGroupLessons);
        lessonAdapter = new TeacherLessonAdapter(this::openAttendance);

        backText.setOnClickListener(v -> showGroupsScreenFromCacheOrApi());

        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            return view;
        }

        authHeader = "Bearer " + token;

        showGroupsScreenFromCacheOrApi();

        return view;
    }

    private void showGroupsScreenFromCacheOrApi() {
        selectedGroup = null;

        backText.setVisibility(View.GONE);
        titleText.setText("Журнал");
        subtitleText.setText("Выберите группу, чтобы открыть занятия");
        recyclerView.setAdapter(groupAdapter);

        emptyText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        if (journalStorage.hasGroups()) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<GroupDto>>() {}.getType();
                List<GroupDto> cachedGroups = gson.fromJson(journalStorage.getGroupsJson(), type);

                if (cachedGroups != null && !cachedGroups.isEmpty()) {
                    groupAdapter.updateGroups(cachedGroups);
                    return;
                }
            } catch (Exception e) {
                journalStorage.clear();
            }
        }

        loadGroupsFromApi();
    }

    private void loadGroupsFromApi() {
        showLoading("Загрузка групп...");

        GroupApi groupApi = ApiClient.getClient().create(GroupApi.class);

        groupApi.getTeacherGroups(authHeader).enqueue(new Callback<List<GroupDto>>() {
            @Override
            public void onResponse(Call<List<GroupDto>> call, Response<List<GroupDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<GroupDto> groups = response.body();

                    Gson gson = new Gson();
                    journalStorage.saveGroupsJson(gson.toJson(groups));

                    if (groups.isEmpty()) {
                        showEmpty("У преподавателя пока нет групп");
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.GONE);
                        groupAdapter.updateGroups(groups);
                    }
                } else {
                    showEmpty("Не удалось загрузить группы. Код: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GroupDto>> call, Throwable t) {
                if (!isAdded()) return;
                showEmpty("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void openGroupLessons(GroupDto group) {
        selectedGroup = group;

        backText.setVisibility(View.VISIBLE);
        titleText.setText(safe(group.getCourseName()));
        subtitleText.setText("Выберите занятие, чтобы открыть посещаемость");

        recyclerView.setAdapter(lessonAdapter);
        emptyText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        String groupId = group.getGroupId();

        if (journalStorage.hasLessonsForGroup(groupId)) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<LessonDto>>() {}.getType();
                List<LessonDto> cachedLessons = gson.fromJson(journalStorage.getLessonsJson(groupId), type);

                if (cachedLessons != null && !cachedLessons.isEmpty()) {
                    lessonAdapter.updateLessons(cachedLessons);
                    return;
                }
            } catch (Exception e) {
                journalStorage.clear();
            }
        }

        loadLessonsFromApi(group);
    }

    private void loadLessonsFromApi(GroupDto group) {
        showLoading("Загрузка занятий...");

        ScheduleApi scheduleApi = ApiClient.getClient().create(ScheduleApi.class);
        String groupId = group.getGroupId();

        scheduleApi.getGroupSchedule(authHeader, groupId).enqueue(new Callback<List<LessonDto>>() {
            @Override
            public void onResponse(Call<List<LessonDto>> call, Response<List<LessonDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<LessonDto> lessons = response.body();

                    Gson gson = new Gson();
                    journalStorage.saveLessonsJson(groupId, gson.toJson(lessons));

                    if (lessons.isEmpty()) {
                        showEmpty("Занятий для этой группы пока нет");
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.GONE);
                        lessonAdapter.updateLessons(lessons);
                    }
                } else {
                    showEmpty("Не удалось загрузить занятия. Код: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<LessonDto>> call, Throwable t) {
                if (!isAdded()) return;
                showEmpty("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void openAttendance(LessonDto lesson) {
        if (selectedGroup == null) {
            Toast.makeText(requireContext(), "Не найдена группа занятия", Toast.LENGTH_SHORT).show();
            return;
        }

        TeacherAttendanceFragment fragment = TeacherAttendanceFragment.newInstance(
                lesson.getId(),
                safe(selectedGroup.getCourseName()),
                formatDate(lesson.getStartTime()) + " • " +
                        formatTime(lesson.getStartTime()) + "–" +
                        formatTime(lesson.getEndTime())
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.teacherFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showLoading(String message) {
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(message);
    }

    private void showEmpty(String message) {
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(message);
    }

    private String formatDate(String dateTime) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("d MMMM", new Locale("ru"));
            return output.format(input.parse(dateTime));
        } catch (ParseException | NullPointerException e) {
            return safe(dateTime);
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

    private String safe(String value) {
        return value == null ? "" : value;
    }
}