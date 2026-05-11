package com.github.nsu_upprpo.school_app.api;

import com.github.nsu_upprpo.school_app.model.AttendanceDto;
import com.github.nsu_upprpo.school_app.model.AttendanceRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
public interface AttendanceApi {
    @GET("lessons/{lesson_id}/attendances")
    Call<List<AttendanceDto>> getLessonAttendances(
            @Header("Authorization") String authHeader,
            @Path("lesson_id") String lessonId
    );

    @POST("lessons/{lesson_id}/attendances")
    Call<AttendanceDto> markAttendance(
            @Header("Authorization") String authHeader,
            @Path("lesson_id") String lessonId,
            @Body AttendanceRequest request
    );
}
