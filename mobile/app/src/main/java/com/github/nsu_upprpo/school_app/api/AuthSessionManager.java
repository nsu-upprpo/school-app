package com.github.nsu_upprpo.school_app.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.github.nsu_upprpo.school_app.BuildConfig;
import com.github.nsu_upprpo.school_app.LoginActivity;
import com.github.nsu_upprpo.school_app.SchoolApp;
import com.github.nsu_upprpo.school_app.model.RefreshRequest;
import com.github.nsu_upprpo.school_app.model.RefreshResponse;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class AuthSessionManager {
    private static final Object REFRESH_LOCK = new Object();
    private static boolean loginRedirectStarted;

    private AuthSessionManager() {
    }

    public static String getAccessToken() {
        Context context = SchoolApp.getAppContext();
        if (context == null) {
            return null;
        }

        return new TokenStorage(context).getAccessToken();
    }

    public static String refreshAccessToken(String failedAccessToken) {
        Context context = SchoolApp.getAppContext();
        if (context == null) {
            return null;
        }

        TokenStorage tokenStorage = new TokenStorage(context);

        synchronized (REFRESH_LOCK) {
            String currentAccessToken = tokenStorage.getAccessToken();
            if (currentAccessToken != null && !currentAccessToken.isEmpty()
                    && failedAccessToken != null
                    && !currentAccessToken.equals(failedAccessToken)) {
                return currentAccessToken;
            }

            String refreshToken = tokenStorage.getRefreshToken();
            if (refreshToken == null || refreshToken.isEmpty()) {
                clearAndOpenLogin();
                return null;
            }

            try {
                Retrofit refreshRetrofit = new Retrofit.Builder()
                        .baseUrl(BuildConfig.BASE_URL)
                        .client(new OkHttpClient.Builder().build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                AuthApi authApi = refreshRetrofit.create(AuthApi.class);
                Response<RefreshResponse> response =
                        authApi.refresh(new RefreshRequest(refreshToken)).execute();

                if (!response.isSuccessful()) {
                    if (isInvalidRefreshResponse(response)) {
                        clearAndOpenLogin();
                    }
                    return null;
                }

                if (response.body() == null
                        || response.body().getAccessToken() == null
                        || response.body().getAccessToken().isEmpty()) {
                    return null;
                }

                String newAccessToken = response.body().getAccessToken();
                String newRefreshToken = response.body().getRefreshToken();
                tokenStorage.saveTokens(
                        newAccessToken,
                        newRefreshToken == null || newRefreshToken.isEmpty()
                                ? refreshToken
                                : newRefreshToken
                );
                loginRedirectStarted = false;
                return newAccessToken;
            } catch (IOException e) {
                return null;
            } catch (Exception e) {
                if (isInvalidRefreshException(e)) {
                    clearAndOpenLogin();
                }
                return null;
            }
        }
    }

    public static void onLoginSuccess() {
        synchronized (REFRESH_LOCK) {
            loginRedirectStarted = false;
        }
    }

    public static boolean isLoginRedirectStarted() {
        synchronized (REFRESH_LOCK) {
            return loginRedirectStarted;
        }
    }

    public static void clearAndOpenLogin() {
        Context context = SchoolApp.getAppContext();
        if (context == null) {
            return;
        }

        synchronized (REFRESH_LOCK) {
            if (loginRedirectStarted) {
                return;
            }
            loginRedirectStarted = true;
            new TokenStorage(context).clear();
        }

        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, "Сессия истекла. Войдите снова", Toast.LENGTH_LONG).show()
        );
        context.startActivity(intent);
    }

    private static boolean isInvalidRefreshResponse(Response<?> response) {
        int code = response.code();
        if (code == 401 || code == 403) {
            return true;
        }

        String errorBody = "";
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (IOException ignored) {
        }

        String normalizedBody = errorBody.toLowerCase();
        return code == 400
                && normalizedBody.contains("refresh")
                && (normalizedBody.contains("invalid") || normalizedBody.contains("expired"));
    }

    private static boolean isInvalidRefreshException(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        String normalizedMessage = message.toLowerCase();
        return normalizedMessage.contains("refresh")
                && (normalizedMessage.contains("invalid") || normalizedMessage.contains("expired"));
    }
}
