package com.example.schoolapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.schoolapp.api.ApiClient;
import com.example.schoolapp.api.AuthApi;
import com.example.schoolapp.api.UserApi;
import com.example.schoolapp.model.LoginRequest;
import com.example.schoolapp.model.LoginResponse;
import com.example.schoolapp.model.RefreshRequest;
import com.example.schoolapp.model.RefreshResponse;
import com.example.schoolapp.model.UserProfile;
import com.example.schoolapp.storage.TokenStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText emailInput;
    EditText passwordInput;
    Button loginButton;

    // позже удалить
    Button testTeacherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TokenStorage storage = new TokenStorage(this);
        String token = storage.getAccessToken();

        if (token != null && !token.isEmpty()) {
            loadProfileAndOpenScreen();
            return;
        }

        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(e -> login());

        // позже удалить
        testTeacherButton = findViewById(R.id.testTeacherButton);
        testTeacherButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, TeacherMainActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();

                    TokenStorage storage = new TokenStorage(LoginActivity.this);
                    storage.saveTokens(body.getAccessToken(), body.getRefreshToken());

                    loadProfileAndOpenScreen();
                } else {
                    String errorText = "Ошибка login. Код: " + response.code();

                    try {
                        if (response.errorBody() != null) {
                            errorText += "\n" + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorText += "\nНе удалось прочитать errorBody";
                    }

                    Toast.makeText(LoginActivity.this, errorText, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadProfileAndOpenScreen() {
        TokenStorage storage = new TokenStorage(this);
        String token = storage.getAccessToken();

        if (token == null || token.isEmpty()) {
            showLoginScreen();
            return;
        }

        String authHeader = "Bearer " + token;

        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        userApi.getMyProfile(authHeader).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();
                    openMainScreenByRole(profile.getRole());
                } else {
                    String errorText = "Ошибка входа. Код: " + response.code();

                    try {
                        if (response.errorBody() != null) {
                            errorText += "\n" + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorText += "\nНе удалось прочитать errorBody";
                    }

                    Toast.makeText(LoginActivity.this, errorText, Toast.LENGTH_LONG).show();
                    showLoginScreen();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void refreshTokenAndRetry() {
        TokenStorage storage = new TokenStorage(this);
        String refreshToken = storage.getRefreshToken();

        if (refreshToken == null || refreshToken.isEmpty()) {
            storage.clear();
            showLoginScreen();
            return;
        }

        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);
        RefreshRequest request = new RefreshRequest(refreshToken);

        authApi.refresh(request).enqueue(new Callback<RefreshResponse>() {
            @Override
            public void onResponse(Call<RefreshResponse> call, Response<RefreshResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RefreshResponse body = response.body();

                    String newAccessToken = body.getAccessToken();
                    String newRefreshToken = body.getRefreshToken();

                    if (newRefreshToken == null || newRefreshToken.isEmpty()) {
                        newRefreshToken = refreshToken;
                    }

                    storage.saveTokens(newAccessToken, newRefreshToken);
                    loadProfileAndOpenScreen();
                } else {
                    storage.clear();
                    Toast.makeText(LoginActivity.this, "Сессия истекла. Войдите заново", Toast.LENGTH_SHORT).show();
                    showLoginScreen();
                }
            }

            @Override
            public void onFailure(Call<RefreshResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Ошибка обновления токена: " + t.getMessage(), Toast.LENGTH_LONG).show();
                showLoginScreen();
            }
        });
    }

    private void showLoginScreen() {
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(e -> login());

        // позже удалить
        testTeacherButton = findViewById(R.id.testTeacherButton);
        testTeacherButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, TeacherMainActivity.class);
            startActivity(intent);
        });
    }
    private void openMainScreenByRole(String role) {
        Intent intent;

        if ("TEACHER".equalsIgnoreCase(role)) {
            intent = new Intent(this, TeacherMainActivity.class);
        } else if ("PARENT".equalsIgnoreCase(role)) {
            intent = new Intent(this, ParentMainActivity.class);
        } else {
            Toast.makeText(this, "Неизвестная роль: " + role, Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(intent);
        finish();
    }
}
