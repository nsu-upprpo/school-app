package com.github.nsu_upprpo.school_app.api;

import com.github.nsu_upprpo.school_app.model.LessonDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
public interface ScheduleApi {
    @GET("schedule/groups/{group_id}")
    Call<List<LessonDto>> getGroupSchedule(
            @Header("Authorization") String authHeader,
            @Path("group_id") String groupId
    );
}
