package com.example.conscia.data.remote.api

import com.example.conscia.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ConsciaApiService {
    // --- Auth ---
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>

    // --- User ---
    @GET("users/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserData>>

    @PATCH("users/profile")
    suspend fun updateUserProfile(
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ApiResponse<UserData>>

    @PATCH("users/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<UserData>>

    // --- Device ---
    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegisterRequest): Response<ApiResponse<DeviceData>>

    @GET("devices")
    suspend fun getDevices(): Response<ApiResponse<List<DeviceData>>>

    // --- Rules & Sessions ---
    @GET("purpose-tags")
    suspend fun getPurposeTags(): Response<ApiResponse<List<PurposeTag>>>

    @POST("tracking-rules")
    suspend fun upsertTrackingRule(@Body request: TrackingRuleRequest): Response<ApiResponse<TrackingRule>>

    @GET("tracking-rules")
    suspend fun getTrackingRules(
        @Query("deviceId") deviceId: String
    ): Response<ApiResponse<List<TrackingRule>>>

    @DELETE("tracking-rules")
    suspend fun deleteTrackingRule(
        @Query("deviceId") deviceId: String,
        @Query("packageName") packageName: String
    ): Response<ApiResponse<Unit>>

    @POST("sessions/sync")
    suspend fun syncSessions(@Body request: SyncSessionsBatchRequest): Response<ApiResponse<SyncSessionsResult>>

    // --- Intentions ---
    @GET("intentions")
    suspend fun getIntentions(): Response<ApiResponse<List<IntentionRemote>>>

    @POST("intentions")
    suspend fun createIntention(@Body request: Map<String, String>): Response<ApiResponse<IntentionRemote>>
    
    @DELETE("intentions/{id}")
    suspend fun deleteIntention(@Path("id") id: String): Response<ApiResponse<Unit>>

    // --- Insights & Stats ---
    @GET("stats/usage-by-purpose")
    suspend fun getUsageByPurpose(
        @Query("deviceId") deviceId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("period") period: String? = null,
        @Query("date") date: String? = null,
        @Query("timezone") timezone: String? = null
    ): Response<ApiResponse<InsightResponse>>

    @GET("stats/daily")
    suspend fun getDailyStats(
        @Query("deviceId") deviceId: String,
        @Query("date") date: String
    ): Response<ApiResponse<DailyStatsResponse>>
}
