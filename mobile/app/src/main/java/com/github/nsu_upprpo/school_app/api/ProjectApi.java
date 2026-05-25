package com.github.nsu_upprpo.school_app.api;

import com.github.nsu_upprpo.school_app.model.ProjectDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;

import com.github.nsu_upprpo.school_app.model.GradeDto;

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

    @GET("projects/{projectId}/grades")
    Call<List<GradeDto>> getProjectGrades(
            @Header("Authorization") String authHeader,
            @Path("projectId") String projectId
    );

    @POST("projects/{projectId}/grades")
    Call<GradeDto> saveProjectGrade(
            @Header("Authorization") String authHeader,
            @Path("projectId") String projectId,
            @Query("childId") String childId,
            @Query("score") int score,
            @Query("comment") String comment
    );
}
