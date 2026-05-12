package com.example.conscia.data.remote.dto

data class SyncSessionsBatchRequest(
    val sessions: List<SyncSessionPayload> = emptyList()
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
    val deviceTimezone: String? = null,
    val timezoneOffsetMinutes: Int? = null,
    val intentionLabel: String? = null,
    val trackingEnabled: Boolean = false,
    val warningEnabled: Boolean = false,
    val dailyLimitMinutes: Int? = null,
    val tags: List<String> = emptyList(),
    val isClassified: Boolean = false
)

data class SyncSessionsResult(
    val processedCount: Int = 0,
    val insertedCount: Int = 0,
    val updatedCount: Int = 0,
    val matchedCount: Int = 0
)
