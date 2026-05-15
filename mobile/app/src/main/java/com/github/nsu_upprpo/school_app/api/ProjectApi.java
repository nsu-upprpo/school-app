package com.github.nsu_upprpo.school_app.api;

import com.github.nsu_upprpo.school_app.model.ProjectDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProjectApi {

    @GET("projects")
    Call<List<ProjectDto>> getProjectsByGroup(
            @Header("Authorization") String authHeader,
            @Query("groupId") String groupId
    );

    @GET("projects/{id}")
    Call<ProjectDto> getProjectById(
            @Header("Authorization") String authHeader,
            @Path("id") String id
    );
}
