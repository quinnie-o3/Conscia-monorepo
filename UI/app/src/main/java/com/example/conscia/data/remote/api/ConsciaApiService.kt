package com.example.conscia.data.remote.api

import com.example.conscia.data.remote.dto.*
import retrofit2.Call
import retrofit2.http.*

interface ConsciaApiService {
    // 1. Register Device
    @POST("devices/register")
    fun registerDevice(@Body request: DeviceRegisterRequest): Call<ApiResponse<DeviceData>>

    // 2. Get Devices
    @GET("devices")
    fun getDevices(): Call<ApiResponse<List<DeviceData>>>

    // 3. Get Purpose Tags
    @GET("purpose-tags")
    fun getPurposeTags(): Call<ApiResponse<List<PurposeTag>>>

    // 4. Create or Update Tracking Rule
    @POST("tracking-rules")
    fun upsertTrackingRule(@Body request: TrackingRuleRequest): Call<ApiResponse<TrackingRule>>

    // 5. Get Tracking Rules
    @GET("tracking-rules")
    fun getTrackingRules(
        @Query("anonymousUserId") anonymousUserId: String?,
        @Query("deviceId") deviceId: String
    ): Call<ApiResponse<List<TrackingRule>>>

    // 6. Delete Tracking Rule
    @DELETE("tracking-rules")
    fun deleteTrackingRule(
        @Query("anonymousUserId") anonymousUserId: String?,
        @Query("deviceId") deviceId: String,
        @Query("packageName") packageName: String
    ): Call<ApiResponse<Unit>>

    // 7. Sync Usage Sessions
    @POST("sessions/sync")
    fun syncSessions(@Body request: SyncSessionsBatchRequest): Call<ApiResponse<SyncSessionsResult>>

    // 8. Usage by purpose (Insights)
    @GET("stats/usage-by-purpose")
    fun getUsageByPurpose(
        @Query("deviceId") deviceId: String,
        @Query("anonymousUserId") anonymousUserId: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("period") period: String? = null,
        @Query("date") date: String? = null,
        @Query("timezone") timezone: String? = null,
        @Header("x-device-id") headerDeviceId: String? = null,
        @Header("x-timezone") headerTimezone: String? = null
    ): Call<ApiResponse<InsightResponse>>

    // 9. Daily stats
    @GET("stats/daily")
    fun getDailyStats(
        @Query("deviceId") deviceId: String,
        @Query("anonymousUserId") anonymousUserId: String? = null,
        @Query("date") date: String,
        @Header("x-device-id") headerDeviceId: String? = null
    ): Call<ApiResponse<DailyStatsResponse>>
}
