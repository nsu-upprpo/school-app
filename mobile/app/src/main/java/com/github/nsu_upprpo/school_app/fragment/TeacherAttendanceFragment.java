package com.github.nsu_upprpo.school_app.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.AttendanceApi;
import com.github.nsu_upprpo.school_app.api.GroupApi;
import com.github.nsu_upprpo.school_app.model.AttendanceDto;
import com.github.nsu_upprpo.school_app.model.AttendanceRequest;
import com.github.nsu_upprpo.school_app.model.GroupDto;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherAttendanceFragment extends Fragment {

    private static final String ARG_LESSON_ID = "lesson_id";
    private static final String ARG_GROUP_ID = "group_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBTITLE = "subtitle";

    private TextView titleText;
    private TextView subtitleText;
    private ImageView backButton;
    private Button markAllButton;
    private Button sendButton;
    private LinearLayout contentLayout;

    private String lessonId;
    private String groupId;
    private String authHeader;
    private boolean isSubmitted;
    private boolean isDirty;

    private final Map<String, String> selectedStatuses = new HashMap<>();
    private final Map<String, String> oldStatuses = new HashMap<>();

    public static TeacherAttendanceFragment newInstance(String lessonId, String groupId,
                                                        String title, String subtitle) {
        TeacherAttendanceFragment fragment = new TeacherAttendanceFragment();

        Bundle args = new Bundle();
        args.putString(ARG_LESSON_ID, lessonId);
        args.putString(ARG_GROUP_ID, groupId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_SUBTITLE, subtitle);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_teacher_attendance, container, false);

        titleText = view.findViewById(R.id.attendanceTitleText);
        subtitleText = view.findViewById(R.id.attendanceSubtitleText);
        backButton = view.findViewById(R.id.attendanceBackButton);
        markAllButton = view.findViewById(R.id.markAllPresentButton);
        sendButton = view.findViewById(R.id.saveAttendanceButton);
        contentLayout = view.findViewById(R.id.attendanceContentLayout);

        Bundle args = getArguments();

        if (args != null) {
            lessonId = args.getString(ARG_LESSON_ID);
            groupId = args.getString(ARG_GROUP_ID);
            titleText.setText(args.getString(ARG_TITLE, "Посещаемость"));
            subtitleText.setText(args.getString(ARG_SUBTITLE, ""));
        }

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            return view;
        }

        authHeader = "Bearer " + token;

        markAllButton.setOnClickListener(v -> markAllPresent());
        sendButton.setOnClickListener(v -> sendAttendance());
        updateSendButtonState();

        loadAttendance();

        return view;
    }

    private void loadAttendance() {
        if (lessonId == null || lessonId.isEmpty()) {
            showMessage("Не найден id занятия");
            setActionsEnabled(false);
            return;
        }

        showMessage("Загрузка посещаемости...");
        setActionsEnabled(false);

        AttendanceApi api = ApiClient.getClient().create(AttendanceApi.class);

        api.getLessonAttendances(authHeader, lessonId).enqueue(new Callback<List<AttendanceDto>>() {
            @Override
            public void onResponse(Call<List<AttendanceDto>> call, Response<List<AttendanceDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        loadGroupRoster();
                    } else {
                        showAttendance(response.body());
                    }
                } else if (response.code() == 401) {
                    return;
                } else {
                    showLoadError();
                }
            }

            @Override
            public void onFailure(Call<List<AttendanceDto>> call, Throwable t) {
                if (!isAdded()) return;
                showLoadError();
            }
        });
    }

    private void loadGroupRoster() {
        if (groupId == null || groupId.isEmpty()) {
            showNoStudentsMessage();
            setActionsEnabled(false);
            return;
        }

        GroupApi groupApi = ApiClient.getClient().create(GroupApi.class);
        groupApi.getGroupById(authHeader, groupId).enqueue(new Callback<GroupDto>() {
            @Override
            public void onResponse(Call<GroupDto> call, Response<GroupDto> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    showRoster(response.body().getStudents());
                } else if (response.code() == 401) {
                    return;
                } else {
                    showLoadError();
                }
            }

            @Override
            public void onFailure(Call<GroupDto> call, Throwable t) {
                if (!isAdded()) return;
                showLoadError();
            }
        });
    }

    private void showRoster(List<GroupDto.StudentInfo> students) {
        contentLayout.removeAllViews();
        selectedStatuses.clear();
        oldStatuses.clear();
        isDirty = false;
        isSubmitted = false;

        if (students == null || students.isEmpty()) {
            showNoStudentsMessage();
            setActionsEnabled(false);
            return;
        }

        markAllButton.setEnabled(true);

        for (GroupDto.StudentInfo student : students) {
            if (student == null) {
                continue;
            }

            addStudentCard(student.getChildId(), student.getFullName(), "ABSENT");
        }

        if (selectedStatuses.isEmpty()) {
            showNoStudentsMessage();
            setActionsEnabled(false);
            return;
        }

        updateSendButtonState();
    }

    private void showAttendance(List<AttendanceDto> attendances) {
        contentLayout.removeAllViews();
        selectedStatuses.clear();
        oldStatuses.clear();
        isDirty = false;
        isSubmitted = hasSavedAttendance(attendances);

        if (attendances == null || attendances.isEmpty()) {
            showNoStudentsMessage();
            setActionsEnabled(false);
            return;
        }

        markAllButton.setEnabled(true);

        for (AttendanceDto attendance : attendances) {
            addStudentCard(attendance);
        }

        updateSendButtonState();
    }

    private boolean hasSavedAttendance(List<AttendanceDto> attendances) {
        if (attendances == null) {
            return false;
        }

        for (AttendanceDto attendance : attendances) {
            if (attendance == null) {
                continue;
            }

            String status = attendance.getStatus();
            if ("PRESENT".equalsIgnoreCase(status) || "ABSENT".equalsIgnoreCase(status)) {
                return true;
            }
        }

        return false;
    }

    private void addStudentCard(AttendanceDto attendance) {
        String childId = attendance.getChildId();
        String childName = attendance.getChildName();
        String status = attendance.getStatus();

        addStudentCard(childId, childName, status);
    }

    private void addStudentCard(String childId, String childName, String status) {
        if (childId == null || childId.isEmpty()) {
            return;
        }

        if (childName == null || childName.isEmpty()) {
            childName = "Ученик";
        }

        if (!"PRESENT".equalsIgnoreCase(status) && !"ABSENT".equalsIgnoreCase(status)) {
            status = "ABSENT";
        }

        oldStatuses.put(childId, status);
        selectedStatuses.put(childId, status);

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(28, 22, 28, 22);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);

        TextView nameText = createText(childName, 17, true, R.color.text_dark);

        RadioGroup radioGroup = new RadioGroup(requireContext());
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);

        RadioButton presentButton = new RadioButton(requireContext());
        presentButton.setText("Был");
        presentButton.setTag(childId + ":PRESENT");

        RadioButton absentButton = new RadioButton(requireContext());
        absentButton.setText("Не был");
        absentButton.setTag(childId + ":ABSENT");

        radioGroup.addView(presentButton);
        radioGroup.addView(absentButton);

        if ("PRESENT".equalsIgnoreCase(status)) {
            presentButton.setChecked(true);
        } else {
            absentButton.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            View checked = group.findViewById(checkedId);

            if (checked != null && checked.getTag() != null) {
                String[] parts = checked.getTag().toString().split(":");

                if (parts.length == 2) {
                    selectedStatuses.put(parts[0], parts[1]);
                    updateDirtyState();
                }
            }
        });

        card.addView(nameText);
        card.addView(radioGroup);

        contentLayout.addView(card);
    }

    private void updateDirtyState() {
        isDirty = !selectedStatuses.equals(oldStatuses);
        updateSendButtonState();
    }

    private void updateSendButtonState() {
        if (sendButton == null) {
            return;
        }

        sendButton.setText(isSubmitted ? "Переотправить" : "Отправить");
        sendButton.setEnabled(isDirty && !selectedStatuses.isEmpty());
    }

    private void setActionsEnabled(boolean enabled) {
        markAllButton.setEnabled(enabled);
        sendButton.setEnabled(enabled && isDirty && !selectedStatuses.isEmpty());
    }

    private void markAllPresent() {
        for (int i = 0; i < contentLayout.getChildCount(); i++) {
            View cardView = contentLayout.getChildAt(i);

            if (!(cardView instanceof LinearLayout)) {
                continue;
            }

            LinearLayout card = (LinearLayout) cardView;

            for (int j = 0; j < card.getChildCount(); j++) {
                View child = card.getChildAt(j);

                if (!(child instanceof RadioGroup)) {
                    continue;
                }

                RadioGroup group = (RadioGroup) child;

                for (int k = 0; k < group.getChildCount(); k++) {
                    View radio = group.getChildAt(k);

                    if (radio instanceof RadioButton && radio.getTag() != null) {
                        String tag = radio.getTag().toString();

                        if (tag.endsWith(":PRESENT")) {
                            ((RadioButton) radio).setChecked(true);
                        }
                    }
                }
            }
        }
    }

    private void sendAttendance() {
        if (selectedStatuses.isEmpty()) {
            Toast.makeText(requireContext(), "Сначала отметьте посещаемость", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDirty) {
            return;
        }

        sendButton.setEnabled(false);
        sendButton.setText("Отправка...");

        int[] total = {0};
        int[] success = {0};
        int[] errors = {0};

        AttendanceApi api = ApiClient.getClient().create(AttendanceApi.class);

        for (Map.Entry<String, String> entry : selectedStatuses.entrySet()) {
            String childId = entry.getKey();
            String status = entry.getValue();

            if (childId == null || childId.isEmpty() || status == null || status.isEmpty()) {
                continue;
            }

            total[0]++;

            AttendanceRequest request = new AttendanceRequest(childId, status);

            api.markAttendance(authHeader, lessonId, request).enqueue(new Callback<AttendanceDto>() {
                @Override
                public void onResponse(Call<AttendanceDto> call, Response<AttendanceDto> response) {
                    if (response.isSuccessful()) {
                        success[0]++;
                    } else if (response.code() == 401) {
                        return;
                    } else {
                        errors[0]++;
                    }

                    checkAttendanceSendingFinished(total[0], success[0], errors[0]);
                }

                @Override
                public void onFailure(Call<AttendanceDto> call, Throwable t) {
                    errors[0]++;
                    checkAttendanceSendingFinished(total[0], success[0], errors[0]);
                }
            });
        }

        if (total[0] == 0) {
            updateSendButtonState();
            Toast.makeText(requireContext(), "Нет данных для отправки", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAttendanceSendingFinished(int total, int success, int errors) {
        if (success + errors < total) {
            return;
        }

        if (errors == 0) {
            oldStatuses.clear();
            oldStatuses.putAll(selectedStatuses);
            isSubmitted = true;
            isDirty = false;
            updateSendButtonState();
            Toast.makeText(
                    requireContext(),
                    "Посещаемость отправлена",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    requireContext(),
                    "Сохранено: " + success + ", ошибок: " + errors,
                    Toast.LENGTH_LONG
            ).show();
            updateDirtyState();
        }
    }

    private void showMessage(String message) {
        contentLayout.removeAllViews();
        contentLayout.addView(createText(message, 15, false, R.color.hint_gray));
    }

    private void showNoStudentsMessage() {
        showMessage("Для этого занятия пока не добавлены ученики. Проверьте группу занятия или обратитесь к администратору.");
    }

    private void showLoadError() {
        contentLayout.removeAllViews();
        contentLayout.addView(createText(
                "Не удалось загрузить список учеников. Проверьте интернет и попробуйте снова.",
                15,
                false,
                R.color.hint_gray
        ));

        Button retryButton = new Button(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 20, 0, 0);
        retryButton.setLayoutParams(params);
        retryButton.setText("Повторить");
        retryButton.setTextColor(getResources().getColor(android.R.color.white));
        retryButton.setBackgroundResource(R.drawable.bg_button_orange);
        retryButton.setOnClickListener(v -> loadAttendance());

        contentLayout.addView(retryButton);
        setActionsEnabled(false);
    }

    private TextView createText(String text, int size, boolean bold, int colorRes) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(getResources().getColor(colorRes));
        textView.setPadding(0, 3, 0, 3);

        if (bold) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return textView;
    }
}
