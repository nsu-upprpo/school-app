package com.github.nsu_upprpo.school_app.api;

import com.github.nsu_upprpo.school_app.model.LoginRequest;
import com.github.nsu_upprpo.school_app.model.LoginResponse;
import com.github.nsu_upprpo.school_app.model.RefreshRequest;
import com.github.nsu_upprpo.school_app.model.RefreshResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Call<RefreshResponse> refresh(@Body RefreshRequest request);
}