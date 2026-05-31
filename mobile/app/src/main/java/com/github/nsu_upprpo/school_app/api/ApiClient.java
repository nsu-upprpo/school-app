package com.github.nsu_upprpo.school_app.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.util.Log;

import com.github.nsu_upprpo.school_app.BuildConfig;

public class ApiClient {
    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request request = chain.request();

                        if (request.header("Authorization") != null || isAuthEndpoint(request)) {
                            return chain.proceed(request);
                        }

                        String token = AuthSessionManager.getAccessToken();
                        if (token == null || token.isEmpty()) {
                            return chain.proceed(request);
                        }

                        Request authenticatedRequest = request.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();

                        return chain.proceed(authenticatedRequest);
                    })
                    .authenticator((route, response) -> {
                        if (isAuthEndpoint(response.request()) || responseCount(response) >= 2) {
                            return null;
                        }

                        String failedToken = getBearerToken(response.request().header("Authorization"));
                        String newToken = AuthSessionManager.refreshAccessToken(failedToken);

                        if (newToken == null || newToken.isEmpty()) {
                            return null;
                        }

                        return response.request()
                                .newBuilder()
                                .header("Authorization", "Bearer " + newToken)
                                .build();
                    });

            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                        message -> Log.d("OkHttp", redactSecrets(message))
                );
                logging.redactHeader("Authorization");
                logging.redactHeader("Cookie");
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                clientBuilder.addInterceptor(logging);
            }

            OkHttpClient client = clientBuilder.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static boolean isAuthEndpoint(Request request) {
        String path = request.url().encodedPath();
        return path.endsWith("/auth/login") || path.endsWith("/auth/refresh");
    }

    private static int responseCount(Response response) {
        int result = 1;

        while ((response = response.priorResponse()) != null) {
            result++;
        }

        return result;
    }

    private static String getBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        }

        String prefix = "Bearer ";
        if (!authorizationHeader.startsWith(prefix)) {
            return null;
        }

        return authorizationHeader.substring(prefix.length());
    }

    private static String redactSecrets(String message) {
        return message
                .replaceAll("(?i)(Authorization:\\s*Bearer\\s+)[^\\s]+", "$1██")
                .replaceAll("(?i)(\"accessToken\"\\s*:\\s*\")[^\"]+\"", "$1██\"")
                .replaceAll("(?i)(\"refreshToken\"\\s*:\\s*\")[^\"]+\"", "$1██\"")
                .replaceAll("(?i)(\"token\"\\s*:\\s*\")[^\"]+\"", "$1██\"");
    }
}
