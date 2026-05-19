package com.example.conscia.data.remote.dto

import com.google.gson.annotations.SerializedName

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
    val purposeTag: String?,
    val intentionLabel: String?,
    val dailyLimitMinutes: Int,
    val trackingEnabled: Boolean,
    val warningEnabled: Boolean,
    val extensionMinutes: Int? = 0,
    val extensionCount: Int? = 0,
    val lastExtensionDate: String? = ""
)

data class TrackingRuleRequest(
    val deviceId: String,
    val packageName: String,
    val appName: String,
    val purposeTag: String? = null,
    val intentionLabel: String? = null,
    val dailyLimitMinutes: Int,
    val trackingEnabled: Boolean,
    val warningEnabled: Boolean,
    val extensionMinutes: Int? = 0,
    val extensionCount: Int? = 0,
    val lastExtensionDate: String? = null
)

data class DailyStatsResponse(
    val date: String,
    val totalDurationSeconds: Long,
    val totalUsedMinutes: Int,
    val totalTrackedMinutes: Int,
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
