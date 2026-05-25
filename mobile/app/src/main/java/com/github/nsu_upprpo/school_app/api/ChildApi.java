package com.github.nsu_upprpo.school_app.api;

import com.github.nsu_upprpo.school_app.model.ChildDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface ChildApi {
    @GET("parents/me/children")
    Call<List<ChildDto>> getMyChildren(@Header("Authorization") String authHeader);

    @GET("parents/me/children/{childId}")
    Call<ChildDto> getChild(
            @Header("Authorization") String authHeader,
            @Path("childId") String childId
    );
}
