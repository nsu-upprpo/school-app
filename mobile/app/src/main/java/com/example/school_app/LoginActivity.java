package com.example.school_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText emailInput;
    EditText passwordInput;
    Button loginButton;

    final String PARENT_EMAIL = "parent@mail.ru";
    final String TEACHER_EMAIL = "teacher@mail.ru";
    final String DEFAULT_PASSWORD = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(e -> login());
    }

    private void login() {
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if(email.equals(PARENT_EMAIL) && password.equals(DEFAULT_PASSWORD)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("role", "parent");
            startActivity(intent);
        } else if (email.equals(TEACHER_EMAIL) && password.equals(DEFAULT_PASSWORD)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("role", "teacher");
            startActivity(intent);
        } else {
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        emailInput.setText("");
        passwordInput.setText("");
    }
}
