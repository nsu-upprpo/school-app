package com.github.nsu_upprpo.school_app.api;

import com.github.nsu_upprpo.school_app.model.GroupDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface GroupApi {
    @GET("parents/me/groups")
    Call<List<GroupDto>> getParentGroups(@Header("Authorization") String authHeader);

    @GET("teachers/me/groups")
    Call<List<GroupDto>> getTeacherGroups(@Header("Authorization") String authHeader);

    @GET("groups/{id}")
    Call<GroupDto> getGroupById(
            @Header("Authorization") String authHeader,
            @Path("id") String id
    );
}