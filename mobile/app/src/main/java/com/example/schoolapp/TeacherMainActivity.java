package com.example.schoolapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.schoolapp.fragment.*;
import com.example.schoolapp.storage.TokenStorage;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TeacherMainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_main);

        bottomNavigationView = findViewById(R.id.teacherBottomNav);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.teacher_nav_journal) {
                fragment = new TeacherJournalFragment();
            } else if (itemId == R.id.teacher_nav_schedule) {
                fragment = new TeacherScheduleFragment();
            } else if (itemId == R.id.teacher_nav_profile) {
                fragment = new TeacherProfileFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.teacherFragmentContainer, fragment)
                        .commit();
                return true;
            }

            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.teacher_nav_schedule);
        }
    }
}
