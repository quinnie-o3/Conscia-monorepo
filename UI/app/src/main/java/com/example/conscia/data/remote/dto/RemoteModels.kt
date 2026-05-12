package com.example.conscia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

data class DeviceData(
    @SerializedName("_id") val id: String?,
    val anonymousUserId: String?,
    val deviceId: String,
    val deviceName: String?,
    val osVersion: String?,
    val platform: String?,
    val isActive: Boolean?,
    val lastSyncAt: String?
)

data class PurposeTag(
    val id: String,
    val name: String,
    val description: String?
)

data class TrackingRule(
    @SerializedName("_id") val id: String?,
    val anonymousUserId: String?,
    val deviceId: String,
    val packageName: String,
    val appName: String,
    val purposeTag: String,
    val dailyLimitMinutes: Int,
    val trackingEnabled: Boolean,
    val warningEnabled: Boolean
)

data class TrackingRuleRequest(
    val anonymousUserId: String? = null,
    val deviceId: String,
    val packageName: String,
    val appName: String,
    val purposeTag: String,
    val dailyLimitMinutes: Int,
    val trackingEnabled: Boolean,
    val warningEnabled: Boolean
)

data class SyncSessionPayload(
    val externalId: String,
    val deviceId: String,
    val packageName: String,
    val appName: String,
    val startedAt: String,
    val endedAt: String,
    val durationSeconds: Long,
    val deviceLocalDate: String,
    val deviceTimezone: String?,
    val timezoneOffsetMinutes: Int?,
    val intentionLabel: String?,
    val trackingEnabled: Boolean,
    val warningEnabled: Boolean,
    val dailyLimitMinutes: Int?,
    val tags: List<String> = emptyList(),
    val isClassified: Boolean = false
)

data class SyncSessionsBatchRequest(
    val anonymousUserId: String? = null,
    val deviceId: String? = null,
    val sessions: List<SyncSessionPayload>
)

data class SyncSessionsResult(
    val processedCount: Int,
    val insertedCount: Int,
    val updatedCount: Int,
    val matchedCount: Int
)

data class DailyStatsResponse(
    val date: String,
    val totalDurationSeconds: Long,
    val totalUsedMinutes: Int,
    val byPurpose: List<PurposeStats>,
    val byApp: List<AppUsageStats>,
    val limitWarnings: List<LimitWarning>
)

data class PurposeStats(
    val purposeTag: String,
    val durationSeconds: Long,
    val usedMinutes: Int,
    val percentage: Double
)

data class AppUsageStats(
    val packageName: String,
    val appName: String,
    val purposeTag: String?,
    val durationSeconds: Long,
    val usedMinutes: Int,
    val limitMinutes: Int?,
    val isExceeded: Boolean
)

data class LimitWarning(
    val packageName: String,
    val appName: String,
    val usedMinutes: Int,
    val limitMinutes: Int,
    val exceededByMinutes: Int
)
