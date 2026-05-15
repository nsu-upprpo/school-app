package com.github.nsu_upprpo.school_app.api;

import com.github.nsu_upprpo.school_app.model.PaymentDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PaymentApi {

    @GET("payments")
    Call<List<PaymentDto>> getMyPayments(
            @Header("Authorization") String authHeader
    );

    @GET("payments")
    Call<List<PaymentDto>> getMyPaymentsByStatus(
            @Header("Authorization") String authHeader,
            @Query("status") String status
    );

    @POST("payments/{payment_id}/submit")
    Call<PaymentDto> submitPayment(
            @Header("Authorization") String authHeader,
            @Path("payment_id") String paymentId
    );
}