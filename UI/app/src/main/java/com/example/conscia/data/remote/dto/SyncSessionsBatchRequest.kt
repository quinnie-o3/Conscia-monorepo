package com.example.conscia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SyncSessionsBatchRequest(
    val deviceId: String,
    val sessions: List<SyncSessionPayload> = emptyList()
)

data class SyncSessionPayload(
    val externalId: String? = null,
    val deviceId: String? = null,
    val packageName: String,
    val appName: String? = null,
    val startedAt: String? = null,
    val endedAt: String? = null,
    val durationSeconds: Long,
    val deviceLocalDate: String? = null,
    val deviceTimezone: String? = null,
    val timezoneOffsetMinutes: Int? = null,
    val intentionLabel: String? = null,
    val trackingEnabled: Boolean? = null,
    val warningEnabled: Boolean? = null,
    val dailyLimitMinutes: Int? = null,
    val tags: List<String> = emptyList(),
    val isClassified: Boolean? = null
)

data class SyncSessionsResult(
    val processedCount: Int = 0,
    val insertedCount: Int = 0,
    val updatedCount: Int = 0,
    val matchedCount: Int = 0
)
