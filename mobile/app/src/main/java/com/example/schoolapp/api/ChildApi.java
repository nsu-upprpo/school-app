package com.example.schoolapp.api;

import com.example.schoolapp.model.ChildDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ChildApi {
    @GET("parents/me/children")
    Call<List<ChildDto>> getMyChildren(@Header("Authorization") String authHeader);
}
