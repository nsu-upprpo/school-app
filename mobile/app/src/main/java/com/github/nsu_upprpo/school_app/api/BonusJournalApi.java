package com.github.nsu_upprpo.school_app.api;

import com.github.nsu_upprpo.school_app.model.BonusJournalDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface BonusJournalApi {
    @GET("parents/me/children/{childId}/bonus-journal")
    Call<List<BonusJournalDto>> getChildBonusJournal(
            @Header("Authorization") String authHeader,
            @Path("childId") String childId
    );
}
