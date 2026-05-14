package com.github.nsu_upprpo.school_app.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.github.nsu_upprpo.school_app.model.AttendanceDto;
import com.github.nsu_upprpo.school_app.model.AttendanceRequest;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherAttendanceFragment extends Fragment {

    private static final String ARG_LESSON_ID = "lesson_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBTITLE = "subtitle";

    private TextView titleText;
    private TextView subtitleText;
    private Button markAllButton;
    private Button sendButton;
    private LinearLayout contentLayout;

    private String lessonId;
    private String authHeader;

    private final Map<String, String> selectedStatuses = new HashMap<>();
    private final Map<String, String> oldStatuses = new HashMap<>();

    public static TeacherAttendanceFragment newInstance(String lessonId, String title, String subtitle) {
        TeacherAttendanceFragment fragment = new TeacherAttendanceFragment();

        Bundle args = new Bundle();
        args.putString(ARG_LESSON_ID, lessonId);
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
        markAllButton = view.findViewById(R.id.markAllPresentButton);
        sendButton = view.findViewById(R.id.saveAttendanceButton);
        contentLayout = view.findViewById(R.id.attendanceContentLayout);

        Bundle args = getArguments();

        if (args != null) {
            lessonId = args.getString(ARG_LESSON_ID);
            titleText.setText(args.getString(ARG_TITLE, "Посещаемость"));
            subtitleText.setText(args.getString(ARG_SUBTITLE, ""));
        }

        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            return view;
        }

        authHeader = "Bearer " + token;

        markAllButton.setOnClickListener(v -> markAllPresent());
        sendButton.setOnClickListener(v -> sendAttendance());

        loadAttendance();

        return view;
    }

    private void loadAttendance() {
        if (lessonId == null || lessonId.isEmpty()) {
            showMessage("Не найден id занятия");
            sendButton.setEnabled(false);
            markAllButton.setEnabled(false);
            return;
        }

        showMessage("Загрузка посещаемости...");

        AttendanceApi api = ApiClient.getClient().create(AttendanceApi.class);

        api.getLessonAttendances(authHeader, lessonId).enqueue(new Callback<List<AttendanceDto>>() {
            @Override
            public void onResponse(Call<List<AttendanceDto>> call, Response<List<AttendanceDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    showAttendance(response.body());
                } else {
                    showMessage("Не удалось загрузить посещаемость. Код: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<AttendanceDto>> call, Throwable t) {
                if (!isAdded()) return;
                showMessage("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void showAttendance(List<AttendanceDto> attendances) {
        contentLayout.removeAllViews();
        selectedStatuses.clear();
        oldStatuses.clear();

        if (attendances == null || attendances.isEmpty()) {
            showMessage("Сервер не вернул список учеников для этого занятия.");
            sendButton.setEnabled(true);
            markAllButton.setEnabled(false);
            return;
        }

        sendButton.setEnabled(true);
        markAllButton.setEnabled(true);

        for (AttendanceDto attendance : attendances) {
            addStudentCard(attendance);
        }
    }

    private void addStudentCard(AttendanceDto attendance) {
        String childId = attendance.getChildId();
        String childName = attendance.getChildName();
        String status = attendance.getStatus();

        if (childId == null || childId.isEmpty()) {
            return;
        }

        if (childName == null || childName.isEmpty()) {
            childName = "Ученик";
        }

        if (status == null) {
            status = "";
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
        } else if ("ABSENT".equalsIgnoreCase(status)) {
            absentButton.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            View checked = group.findViewById(checkedId);

            if (checked != null && checked.getTag() != null) {
                String[] parts = checked.getTag().toString().split(":");

                if (parts.length == 2) {
                    selectedStatuses.put(parts[0], parts[1]);
                }
            }
        });

        card.addView(nameText);
        card.addView(radioGroup);

        contentLayout.addView(card);
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
            sendButton.setEnabled(true);
            sendButton.setText("Отправить");
            Toast.makeText(requireContext(), "Нет данных для отправки", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAttendanceSendingFinished(int total, int success, int errors) {
        if (success + errors < total) {
            return;
        }

        sendButton.setEnabled(true);
        sendButton.setText("Отправить");

        if (errors == 0) {
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
        }
    }

    private void showMessage(String message) {
        contentLayout.removeAllViews();
        contentLayout.addView(createText(message, 15, false, R.color.hint_gray));
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