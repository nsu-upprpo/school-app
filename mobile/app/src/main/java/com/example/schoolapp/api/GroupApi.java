package com.example.schoolapp.api;

import com.example.schoolapp.model.GroupDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface GroupApi {
    @GET("parents/me/groups")
    Call<List<GroupDto>> getParentGroups(@Header("Authorization") String authHeader);

    @GET("teachers/me/groups")
    Call<List<GroupDto>> getTeacherGroups(@Header("Authorization") String authHeader);
}