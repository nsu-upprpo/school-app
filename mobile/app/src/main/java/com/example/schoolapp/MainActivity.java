package com.example.schoolapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.schoolapp.api.ApiClient;
import com.example.schoolapp.api.GroupApi;
import com.example.schoolapp.api.UserApi;
import com.example.schoolapp.model.GroupDto;
import com.example.schoolapp.model.UserProfile;
import com.example.schoolapp.storage.TokenStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    TextView welcomeText;
    TextView infoTitleText;
    TextView infoText;
    Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        welcomeText = findViewById(R.id.welcomeText);
        infoTitleText = findViewById(R.id.infoTitleText);
        infoText = findViewById(R.id.infoText);
        logoutButton = findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(v -> logout());
        loadData();
    }

    private void loadData() {
        TokenStorage storage = new TokenStorage(this);
        String token = storage.getAccessToken();

        if (token == null) {
            logout();
            return;
        }

        String authHeader = "Bearer " + token;

        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        userApi.getMyProfile(authHeader).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();

                    String firstName = profile.getFirstName() != null ? profile.getFirstName() : "";
                    welcomeText.setText("Здравствуйте, " + firstName + "!");
                    infoTitleText.setText("Информация");

                    String role = profile.getRole();

                    if ("PARENT".equalsIgnoreCase(role)) {
                        loadParentGroups(authHeader, profile);
                    } else if ("TEACHER".equalsIgnoreCase(role)) {
                        loadTeacherGroups(authHeader, profile);
                    } else {
                        infoText.setText(
                                "Email: " + safe(profile.getEmail()) + "\n" +
                                        "Роль: " + safe(profile.getRole()) + "\n\n" +
                                        "Неизвестная роль пользователя"
                        );
                    }
                } else {
                    infoText.setText("Не удалось загрузить профиль");
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                infoText.setText("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void loadParentGroups(String authHeader, UserProfile profile) {
        GroupApi groupApi = ApiClient.getClient().create(GroupApi.class);

        groupApi.getParentGroups(authHeader).enqueue(new Callback<List<GroupDto>>() {
            @Override
            public void onResponse(Call<List<GroupDto>> call, Response<List<GroupDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GroupDto> groups = response.body();

                    StringBuilder sb = new StringBuilder();
                    sb.append("Email: ").append(safe(profile.getEmail())).append("\n");
                    sb.append("Роль: ").append(safe(profile.getRole())).append("\n\n");
                    sb.append("Группы ребенка:\n");

                    if (groups.isEmpty()) {
                        sb.append("Нет доступных групп");
                    } else {
                        for (GroupDto group : groups) {
                            sb.append("• ").append(safe(group.getName())).append("\n");
                            if (group.getScheduleDescription() != null && !group.getScheduleDescription().isEmpty()) {
                                sb.append("  ").append(group.getScheduleDescription()).append("\n");
                            }
                        }
                    }

                    infoText.setText(sb.toString());
                } else {
                    infoText.setText("Не удалось загрузить группы родителя");
                }
            }

            @Override
            public void onFailure(Call<List<GroupDto>> call, Throwable t) {
                infoText.setText("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void loadTeacherGroups(String authHeader, UserProfile profile) {
        GroupApi groupApi = ApiClient.getClient().create(GroupApi.class);

        groupApi.getTeacherGroups(authHeader).enqueue(new Callback<List<GroupDto>>() {
            @Override
            public void onResponse(Call<List<GroupDto>> call, Response<List<GroupDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GroupDto> groups = response.body();

                    StringBuilder sb = new StringBuilder();
                    sb.append("Email: ").append(safe(profile.getEmail())).append("\n");
                    sb.append("Роль: ").append(safe(profile.getRole())).append("\n\n");
                    sb.append("Мои группы:\n");

                    if (groups.isEmpty()) {
                        sb.append("Нет доступных групп");
                    } else {
                        for (GroupDto group : groups) {
                            sb.append("• ").append(safe(group.getName())).append("\n");
                            if (group.getScheduleDescription() != null && !group.getScheduleDescription().isEmpty()) {
                                sb.append("  ").append(group.getScheduleDescription()).append("\n");
                            }
                        }
                    }

                    infoText.setText(sb.toString());
                } else {
                    infoText.setText("Не удалось загрузить группы преподавателя");
                }
            }

            @Override
            public void onFailure(Call<List<GroupDto>> call, Throwable t) {
                infoText.setText("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void logout() {
        TokenStorage storage = new TokenStorage(this);
        storage.clear();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}