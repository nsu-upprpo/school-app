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
import com.example.schoolapp.model.UserProfile;
import com.example.schoolapp.storage.TokenStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText emailInput;
    EditText passwordInput;
    Button loginButton;

    //
    Button testTeacherButton;
    Button testParentButton;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        //
        testTeacherButton = findViewById(R.id.testTeacherButton);
        testParentButton = findViewById(R.id.testParentButton);
        //
        loginButton.setOnClickListener(e -> login());

        //
        testTeacherButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, TeacherMainActivity.class);
            startActivity(intent);
        });

        testParentButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ParentMainActivity.class);
            startActivity(intent);
        });
        //
    }

    /*
    1. Вызвали auth/login
    2. Сохранили токен
    3. Вызвали users/me
    4. По роли открыли нужную activity
     */
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
                    Toast.makeText(LoginActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        emailInput.setText("");
        passwordInput.setText("");
    }

    private void loadProfileAndOpenScreen() {
        TokenStorage storage = new TokenStorage(this);
        String token = storage.getAccessToken();

        if (token == null) {
            Toast.makeText(this, "Токен не найден", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(LoginActivity.this, "Не удалось получить профиль", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
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
