package com.github.nsu_upprpo.school_app.api;

import com.github.nsu_upprpo.school_app.model.UserProfile;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserApi {
    @GET("users/me")
    Call<UserProfile> getMyProfile(@Header("Authorization") String authHeader);
}