package com.github.nsu_upprpo.school_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.BonusJournalApi;
import com.github.nsu_upprpo.school_app.api.ChildApi;
import com.github.nsu_upprpo.school_app.api.GroupApi;
import com.github.nsu_upprpo.school_app.model.BonusJournalDto;
import com.github.nsu_upprpo.school_app.model.ChildDto;
import com.github.nsu_upprpo.school_app.model.GroupDto;
import com.github.nsu_upprpo.school_app.storage.ParentChildProfileStorage;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentChildProfileFragment extends Fragment {

    private static final String ARG_CHILD_ID = "child_id";
    private static final String ARG_CHILD_NAME = "child_name";

    private TextView titleText;
    private TextView bonusJournalTitleText;
    private LinearLayout childInfoCard;
    private LinearLayout bonusJournalLayout;

    private String childId;
    private String initialChildName;
    private String authHeader;

    private ChildDto child;
    private ParentChildProfileStorage profileStorage;
    private boolean hasCachedProfile;
    private boolean refreshErrorShown;
    private final Map<String, GroupDto> parentGroupsById = new LinkedHashMap<>();

    public static ParentChildProfileFragment newInstance(String childId, String childName) {
        ParentChildProfileFragment fragment = new ParentChildProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_ID, childId);
        args.putString(ARG_CHILD_NAME, childName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_child_profile, container, false);

        titleText = view.findViewById(R.id.childProfileTitleText);
        bonusJournalTitleText = view.findViewById(R.id.bonusJournalTitleText);
        childInfoCard = view.findViewById(R.id.childInfoCard);
        bonusJournalLayout = view.findViewById(R.id.bonusJournalLayout);

        Bundle args = getArguments();
        if (args != null) {
            childId = args.getString(ARG_CHILD_ID);
            initialChildName = args.getString(ARG_CHILD_NAME);
        }

        titleText.setText(nonEmpty(initialChildName, "Профиль ребёнка"));
        profileStorage = new ParentChildProfileStorage(requireContext());
        loadChildProfile();

        return view;
    }

    private void loadChildProfile() {
        hasCachedProfile = profileStorage.hasChildProfile(childId);

        if (hasCachedProfile) {
            showCachedChildProfile();
        } else {
            showLoadingState();
        }

        refreshChildProfileFromBackend();
    }

    private void showCachedChildProfile() {
        ChildDto cachedChild = profileStorage.getChild(childId);
        List<GroupDto> cachedGroups = profileStorage.getParentGroups();

        applyParentGroups(cachedGroups);

        if (cachedChild != null) {
            child = cachedChild;
            titleText.setText(nonEmpty(child.getFullName(), "Профиль ребёнка"));
            renderChildInfo(child);
        }

        if (profileStorage.hasBonusJournal(childId)) {
            renderBonusJournal(profileStorage.getBonusJournal(childId));
        } else {
            showBonusJournalMessage("Загрузка...");
        }
    }

    private void refreshChildProfileFromBackend() {
        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

        if (token == null || token.isEmpty() || childId == null || childId.isEmpty()) {
            Toast.makeText(requireContext(), "Не удалось открыть профиль ребёнка", Toast.LENGTH_SHORT).show();
            return;
        }

        authHeader = "Bearer " + token;
        refreshErrorShown = false;

        loadChild();
        loadParentGroups();
        loadBonusJournal();
    }

    private void loadChild() {
        ChildApi childApi = ApiClient.getClient().create(ChildApi.class);

        childApi.getChild(authHeader, childId).enqueue(new Callback<ChildDto>() {
            @Override
            public void onResponse(Call<ChildDto> call, Response<ChildDto> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    child = response.body();
                    profileStorage.saveChild(childId, child);
                    titleText.setText(nonEmpty(child.getFullName(), "Профиль ребёнка"));
                    renderChildInfo(child);
                } else {
                    handleRefreshError("Не удалось загрузить ребёнка");
                }
            }

            @Override
            public void onFailure(Call<ChildDto> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }

                handleRefreshError("Ошибка загрузки ребёнка");
            }
        });
    }

    private void loadParentGroups() {
        GroupApi groupApi = ApiClient.getClient().create(GroupApi.class);

        groupApi.getParentGroups(authHeader).enqueue(new Callback<List<GroupDto>>() {
            @Override
            public void onResponse(Call<List<GroupDto>> call, Response<List<GroupDto>> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    profileStorage.saveParentGroups(response.body());
                    applyParentGroups(response.body());

                    if (child != null) {
                        renderChildInfo(child);
                    }
                } else {
                    handleRefreshError("Не удалось обновить группы ребёнка");
                }
            }

            @Override
            public void onFailure(Call<List<GroupDto>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }

                handleRefreshError("Ошибка обновления групп ребёнка");
            }
        });
    }

    private void loadBonusJournal() {
        BonusJournalApi bonusJournalApi = ApiClient.getClient().create(BonusJournalApi.class);

        bonusJournalApi.getChildBonusJournal(authHeader, childId).enqueue(new Callback<List<BonusJournalDto>>() {
            @Override
            public void onResponse(Call<List<BonusJournalDto>> call,
                                   Response<List<BonusJournalDto>> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    profileStorage.saveBonusJournal(childId, response.body());
                    renderBonusJournal(response.body());
                } else {
                    handleBonusJournalError("Не удалось загрузить бонусный журнал");
                }
            }

            @Override
            public void onFailure(Call<List<BonusJournalDto>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }

                handleBonusJournalError("Ошибка загрузки бонусного журнала");
            }
        });
    }

    private void applyParentGroups(List<GroupDto> groups) {
        parentGroupsById.clear();

        if (groups == null) {
            return;
        }

        for (GroupDto group : groups) {
            String groupId = group.getGroupId();
            if (groupId != null && !groupId.isEmpty()) {
                parentGroupsById.put(groupId, group);
            }
        }
    }

    private void handleBonusJournalError(String message) {
        if (hasCachedProfile) {
            handleRefreshError(message);
        } else {
            showBonusJournalMessage(message);
        }
    }

    private void handleRefreshError(String message) {
        if (hasCachedProfile) {
            if (!refreshErrorShown) {
                Toast.makeText(requireContext(), "Не удалось обновить данные, показан кэш", Toast.LENGTH_SHORT).show();
                refreshErrorShown = true;
            }
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoadingState() {
        childInfoCard.removeAllViews();
        addInfoRow(childInfoCard, "Данные", "Загрузка...");
        bonusJournalTitleText.setText("Бонусный журнал");
        showBonusJournalMessage("Загрузка...");
    }

    private void renderChildInfo(ChildDto child) {
        childInfoCard.removeAllViews();

        addInfoRow(childInfoCard, "Дата рождения", formatBirthDate(child.getBirthDate()));
        addInfoRow(childInfoCard, "Возраст", formatAge(child.getBirthDate()));
    }

    private void renderBonusJournal(List<BonusJournalDto> journal) {
        bonusJournalLayout.removeAllViews();

        if (journal == null || journal.isEmpty()) {
            bonusJournalTitleText.setText("Бонусный журнал");
            showBonusJournalMessage("Пока нет данных по бонусному журналу");
            return;
        }

        Map<String, List<BonusJournalDto>> groupedJournal = new LinkedHashMap<>();

        for (BonusJournalDto item : journal) {
            String groupId = safeKey(item.getGroupId());
            String courseName = nonEmpty(item.getCourseName(), "Курс не указан");
            String key = groupId + "|" + courseName;

            if (!groupedJournal.containsKey(key)) {
                groupedJournal.put(key, new ArrayList<>());
            }

            groupedJournal.get(key).add(item);
        }

        bonusJournalTitleText.setText("Бонусный журнал");

        for (Map.Entry<String, List<BonusJournalDto>> entry : groupedJournal.entrySet()) {
            addCourseSection(entry.getKey(), entry.getValue());
        }
    }

    private void addInfoRow(LinearLayout parent, String label, String value) {
        TextView labelText = new TextView(requireContext());
        labelText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        labelText.setText(label);
        labelText.setTextColor(getResources().getColor(R.color.hint_gray));
        labelText.setTextSize(14);

        TextView valueText = new TextView(requireContext());
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        valueParams.setMargins(0, dp(3), 0, dp(10));
        valueText.setLayoutParams(valueParams);
        valueText.setText(nonEmpty(value, "не указано"));
        valueText.setTextColor(getResources().getColor(R.color.text_dark));
        valueText.setTextSize(16);

        parent.addView(labelText);
        parent.addView(valueText);
    }

    private void addCourseSection(String key, List<BonusJournalDto> projects) {
        if (projects == null || projects.isEmpty()) {
            return;
        }

        BonusJournalDto firstProject = projects.get(0);

        LinearLayout courseCard = new LinearLayout(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(14));
        courseCard.setLayoutParams(cardParams);
        courseCard.setOrientation(LinearLayout.VERTICAL);
        courseCard.setPadding(dp(18), dp(16), dp(18), dp(18));
        courseCard.setBackgroundResource(R.drawable.bg_card);

        TextView courseTitle = new TextView(requireContext());
        courseTitle.setText(nonEmpty(firstProject.getCourseName(), "Курс не указан"));
        courseTitle.setTextColor(getResources().getColor(R.color.text_dark));
        courseTitle.setTextSize(20);
        courseTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        courseCard.addView(courseTitle);

        addJournalHeaderRow(courseCard);

        for (int i = 0; i < projects.size(); i++) {
            if (i > 0) {
                addProjectDivider(courseCard);
            }

            addProjectRow(courseCard, projects.get(i));
        }

        if (projects.size() > 1) {
            addCourseSummary(courseCard, projects);
        }

        bonusJournalLayout.addView(courseCard);
    }

    private void addJournalHeaderRow(LinearLayout parent) {
        LinearLayout headerLayout = new LinearLayout(requireContext());
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headerParams.setMargins(0, dp(20), 0, dp(8));
        headerLayout.setLayoutParams(headerParams);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView projectText = createHeaderText("Проект");
        projectText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.35f
        ));

        TextView attendanceText = createHeaderText("Посещение");
        attendanceText.setGravity(android.view.Gravity.CENTER);
        attendanceText.setSingleLine(true);
        attendanceText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0.9f
        ));

        TextView scoreText = createHeaderText("Оценка");
        scoreText.setGravity(android.view.Gravity.END);
        scoreText.setSingleLine(true);
        scoreText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0.75f
        ));

        headerLayout.addView(projectText);
        headerLayout.addView(attendanceText);
        headerLayout.addView(scoreText);
        parent.addView(headerLayout);
    }

    private TextView createHeaderText(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.hint_gray));
        textView.setTextSize(13);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        return textView;
    }

    private void addProjectRow(LinearLayout parent, BonusJournalDto project) {
        LinearLayout rowLayout = new LinearLayout(requireContext());
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, dp(8), 0, dp(8));
        rowLayout.setLayoutParams(rowParams);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        LinearLayout projectInfoLayout = new LinearLayout(requireContext());
        projectInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.35f
        ));
        projectInfoLayout.setOrientation(LinearLayout.VERTICAL);

        TextView projectTitle = new TextView(requireContext());
        projectTitle.setText(nonEmpty(project.getProjectName(), "Проект без названия"));
        projectTitle.setTextColor(getResources().getColor(R.color.text_dark));
        projectTitle.setTextSize(16);
        projectTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        projectInfoLayout.addView(projectTitle);

        TextView attendanceText = new TextView(requireContext());
        attendanceText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0.9f
        ));
        attendanceText.setText(formatAttendanceValue(project));
        attendanceText.setTextColor(getResources().getColor(R.color.text_dark));
        attendanceText.setTextSize(15);
        attendanceText.setGravity(android.view.Gravity.CENTER);
        attendanceText.setSingleLine(true);

        TextView scoreText = new TextView(requireContext());
        scoreText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0.75f
        ));
        scoreText.setText(formatScoreValue(project));
        scoreText.setTextColor(getResources().getColor(R.color.course_orange));
        scoreText.setTextSize(18);
        scoreText.setTypeface(null, android.graphics.Typeface.BOLD);
        scoreText.setGravity(android.view.Gravity.END);
        scoreText.setSingleLine(true);

        rowLayout.addView(projectInfoLayout);
        rowLayout.addView(attendanceText);
        rowLayout.addView(scoreText);
        parent.addView(rowLayout);
    }

    private void addProjectDivider(LinearLayout parent) {
        View divider = new View(requireContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
        );
        dividerParams.setMargins(0, dp(2), 0, dp(2));
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0xFFE6E6E6);
        parent.addView(divider);
    }

    private void addCourseSummary(LinearLayout parent, List<BonusJournalDto> projects) {
        int scoreSum = 0;
        int scoredProjects = 0;
        int visitedLessons = 0;
        int totalLessons = 0;

        for (BonusJournalDto project : projects) {
            if (project.getScore() != null) {
                scoreSum += project.getScore();
                scoredProjects++;
            }

            visitedLessons += project.getVisitedLessons();
            totalLessons += project.getTotalLessons();
        }

        LinearLayout summaryLayout = new LinearLayout(requireContext());
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        summaryParams.setMargins(0, dp(14), 0, 0);
        summaryLayout.setLayoutParams(summaryParams);
        summaryLayout.setOrientation(LinearLayout.VERTICAL);
        summaryLayout.setPadding(dp(14), dp(12), dp(14), dp(12));
        summaryLayout.setBackgroundResource(R.drawable.bg_readonly_field);

        TextView titleText = new TextView(requireContext());
        titleText.setText("Итог по курсу");
        titleText.setTextColor(getResources().getColor(R.color.text_dark));
        titleText.setTextSize(15);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        summaryLayout.addView(titleText);

        if (scoredProjects > 0) {
            addProjectDetail(summaryLayout, "Средний балл: " + (scoreSum / scoredProjects));
        } else {
            addProjectDetail(summaryLayout, "Средний балл: пока нет");
        }

        if (totalLessons > 0) {
            addProjectDetail(summaryLayout, "Посещено: " + visitedLessons + " из " + totalLessons + " занятий");
        } else {
            addProjectDetail(summaryLayout, "Занятий пока не было");
        }

        parent.addView(summaryLayout);
    }

    private void addProjectDetail(LinearLayout parent, String text) {
        TextView textView = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(5), 0, 0);
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.text_dark));
        textView.setTextSize(14);
        parent.addView(textView);
    }

    private void showBonusJournalMessage(String message) {
        bonusJournalLayout.removeAllViews();

        TextView textView = new TextView(requireContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setBackgroundResource(R.drawable.bg_readonly_field);
        textView.setPadding(dp(18), dp(18), dp(18), dp(18));
        textView.setText(message);
        textView.setTextColor(getResources().getColor(R.color.hint_gray));
        textView.setTextSize(16);
        bonusJournalLayout.addView(textView);
    }

    private String formatBirthDate(String birthDate) {
        Date date = parseDate(birthDate);
        if (date == null) {
            return "не указана";
        }

        return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date);
    }

    private String formatAge(String birthDate) {
        Date date = parseDate(birthDate);
        if (date == null) {
            return "не указан";
        }

        Calendar birth = Calendar.getInstance();
        birth.setTime(date);

        Calendar now = Calendar.getInstance();
        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        if (age < 0) {
            return "не указан";
        }

        return age + " " + ageWord(age);
    }

    private String ageWord(int age) {
        int lastDigit = age % 10;
        int lastTwoDigits = age % 100;

        if (lastTwoDigits >= 11 && lastTwoDigits <= 14) {
            return "лет";
        }

        if (lastDigit == 1) {
            return "год";
        }

        if (lastDigit >= 2 && lastDigit <= 4) {
            return "года";
        }

        return "лет";
    }

    private String formatAttendanceValue(BonusJournalDto project) {
        if (project.getTotalLessons() == 0) {
            return "нет";
        }

        return project.getVisitedLessons() + "/" + project.getTotalLessons();
    }

    private String formatScoreValue(BonusJournalDto project) {
        if (project.getScore() == null) {
            return "пока нет";
        }

        if (project.getMaxScore() == null) {
            return String.valueOf(project.getScore());
        }

        return project.getScore() + "/" + project.getMaxScore();
    }

    private Date parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    private String getBranchName(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            return "";
        }

        GroupDto group = parentGroupsById.get(groupId);
        if (group == null || group.getBranchName() == null || group.getBranchName().isEmpty()) {
            return "";
        }

        return group.getBranchName();
    }

    private String nonEmpty(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private String safeKey(String value) {
        return value == null ? "" : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
