package com.example.schoolapp.api;

import com.example.schoolapp.model.BranchDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
public interface BranchApi {
    @GET("branches")
    Call<List<BranchDto>> getAllBranches(@Header("Authorization") String authHeader);

    @GET("branches/{id}")
    Call<BranchDto> getBranchById(@Header("Authorization") String authHeader, @Path("id") String id);
}
