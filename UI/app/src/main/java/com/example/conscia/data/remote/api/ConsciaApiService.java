package com.example.conscia.data.remote.api;

import com.example.conscia.data.remote.dto.ApiResponse;
import com.example.conscia.data.remote.dto.InsightResponse;
import com.example.conscia.data.remote.dto.SyncSessionsBatchRequest;
import com.example.conscia.data.remote.dto.SyncSessionsResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ConsciaApiService {
    @GET("stats/usage-by-purpose")
    Call<ApiResponse<InsightResponse>> getPurposeInsights(
        @Query("deviceId") String deviceId,
        @Query("from") String from,
        @Query("to") String to
    );

    @POST("sessions/sync")
    Call<ApiResponse<SyncSessionsResult>> syncSessions(
        @Body SyncSessionsBatchRequest body
    );
}
