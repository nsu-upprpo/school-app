package com.example.school_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView welcomeText;
    TextView infoTitleText;
    TextView infoText;
    Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcomeText = findViewById(R.id.welcomeText);
        infoTitleText = findViewById(R.id.infoTitleText);
        infoText = findViewById(R.id.infoText);
        logoutButton = findViewById(R.id.logoutButton);
        String role = getIntent().getStringExtra("role");

        if ("parent".equals(role)) {
            welcomeText.setText("Здравствуйте!");
            infoTitleText.setText("Ваши данные");
            infoText.setText(
                    "Филиал: Центральный\n\n" +
                            "Курсы ребенка:\n" +
                            "• Рисование\n" +
                            "• Живопись"
            );
        }
        else if ("teacher".equals(role)) {
            welcomeText.setText("Здравствуйте!");
            infoTitleText.setText("Ваши данные");
            infoText.setText(
                    "Вы ведёте курсы:\n\n" +
                            "• Академический рисунок\n" +
                            "• Живопись"
            );
        }

        logoutButton.setOnClickListener(v -> logout());
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
