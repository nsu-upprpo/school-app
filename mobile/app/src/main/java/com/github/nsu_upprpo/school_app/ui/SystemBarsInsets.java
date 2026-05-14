package com.github.nsu_upprpo.school_app.ui;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public final class SystemBarsInsets {

    private static final String STATUS_BAR_TAG = "status_bar_scrim";

    private SystemBarsInsets() {
    }

    public static void apply(@NonNull AppCompatActivity activity,
                             @NonNull View rootView) {

        EdgeToEdge.enable(activity);

        WindowCompat.getInsetsController(activity.getWindow(), rootView)
                .setAppearanceLightStatusBars(false);

        WindowCompat.getInsetsController(activity.getWindow(), rootView)
                .setAppearanceLightNavigationBars(true);

        int initialLeft = rootView.getPaddingLeft();
        int initialTop = rootView.getPaddingTop();
        int initialRight = rootView.getPaddingRight();
        int initialBottom = rootView.getPaddingBottom();

        FrameLayout content = activity.findViewById(android.R.id.content);

        View statusBarScrim = content.findViewWithTag(STATUS_BAR_TAG);
        if (statusBarScrim == null) {
            statusBarScrim = new View(activity);
            statusBarScrim.setTag(STATUS_BAR_TAG);
            statusBarScrim.setBackgroundColor(Color.BLACK);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0
            );

            content.addView(statusBarScrim, params);
        }

        View finalStatusBarScrim = statusBarScrim;

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            ViewGroup.LayoutParams params = finalStatusBarScrim.getLayoutParams();
            params.height = systemBars.top;
            finalStatusBarScrim.setLayoutParams(params);

            view.setPadding(
                    initialLeft + systemBars.left,
                    initialTop + systemBars.top,
                    initialRight + systemBars.right,
                    initialBottom
            );

            return windowInsets;
        });
    }
}