package com.example.schoolapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.schoolapp.fragment.*;
import com.example.schoolapp.storage.TokenStorage;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ParentMainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_main);

        bottomNavigationView = findViewById(R.id.parentBottomNav);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.parent_nav_lessons) {
                fragment = new ParentLessonsFragment();
            } else if (itemId == R.id.parent_nav_schedule) {
                fragment = new ParentScheduleFragment();
            } else if (itemId == R.id.parent_nav_payment) {
                fragment = new ParentPaymentFragment();
            } else if (itemId == R.id.parent_nav_profile) {
                fragment = new ParentProfileFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.parentFragmentContainer, fragment)
                        .commit();
                return true;
            }

            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.parent_nav_lessons);
        }
    }
}
