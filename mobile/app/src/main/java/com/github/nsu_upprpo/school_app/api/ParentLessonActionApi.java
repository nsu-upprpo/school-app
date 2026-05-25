package com.github.nsu_upprpo.school_app.api;

import com.github.nsu_upprpo.school_app.model.CancelLessonRequest;
import com.github.nsu_upprpo.school_app.model.RescheduleLessonRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ParentLessonActionApi {
    @POST("parents/me/children/{childId}/lessons/{lessonId}/cancel")
    Call<Void> cancelLesson(
            @Header("Authorization") String authHeader,
            @Path("childId") String childId,
            @Path("lessonId") String lessonId,
            @Body CancelLessonRequest request
    );

    @POST("parents/me/children/{childId}/lessons/{lessonId}/reschedule")
    Call<Void> rescheduleLesson(
            @Header("Authorization") String authHeader,
            @Path("childId") String childId,
            @Path("lessonId") String lessonId,
            @Body RescheduleLessonRequest request
    );

    @POST("parents/me/children/{childId}/lessons/{lessonId}/restore")
    Call<Void> restoreLesson(
            @Header("Authorization") String authHeader,
            @Path("childId") String childId,
            @Path("lessonId") String lessonId
    );
}
