package com.github.nsu_upprpo.school_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.AuthApi;
import com.github.nsu_upprpo.school_app.api.UserApi;
import com.github.nsu_upprpo.school_app.model.LoginRequest;
import com.github.nsu_upprpo.school_app.model.LoginResponse;
import com.github.nsu_upprpo.school_app.model.UserProfile;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;
import com.github.nsu_upprpo.school_app.ui.SystemBarsInsets;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText emailInput;
    EditText passwordInput;
    Button loginButton;

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
        setupLoginScreen();
    }

    private void setupLoginScreen() {
        SystemBarsInsets.apply(this, findViewById(R.id.loginRoot));
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(e -> login());
    }

    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Вход...");

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
                    Toast.makeText(LoginActivity.this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
                    loginButton.setEnabled(true);
                    loginButton.setText("Войти");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loginButton.setEnabled(true);
                loginButton.setText("Войти");

                Toast.makeText(LoginActivity.this,"Нет соединения с сервером. Проверьте интернет и попробуйте снова", Toast.LENGTH_LONG).show();

                TokenStorage storage = new TokenStorage(LoginActivity.this);
                storage.clear();

                showLoginScreen();
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
                    Toast.makeText(LoginActivity.this, "Ошибка входа. Попробуйте снова", Toast.LENGTH_SHORT).show();

                    TokenStorage storage = new TokenStorage(LoginActivity.this);
                    storage.clear();

                    showLoginScreen();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                TokenStorage storage = new TokenStorage(LoginActivity.this);
                storage.clear();

                Toast.makeText(LoginActivity.this, "Ошибка соединения. Проверьте интернет", Toast.LENGTH_SHORT).show();

                showLoginScreen();
            }
        });
    }

    private void showLoginScreen() {
        setContentView(R.layout.activity_login);
        setupLoginScreen();
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
