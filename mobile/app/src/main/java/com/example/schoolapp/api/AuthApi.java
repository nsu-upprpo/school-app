package com.example.schoolapp.api;

import com.example.schoolapp.model.LoginRequest;
import com.example.schoolapp.model.LoginResponse;
import com.example.schoolapp.model.RefreshRequest;
import com.example.schoolapp.model.RefreshResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Call<RefreshResponse> refresh(@Body RefreshRequest request);
}