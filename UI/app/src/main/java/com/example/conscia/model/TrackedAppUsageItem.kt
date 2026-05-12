package com.example.conscia.model

data class TrackedAppUsageItem(
    val packageName: String,
    val appName: String,
    val todayUsageMillis: Long,
    val dailyLimitMinutes: Int? = null,
    val intentionLabel: String? = null,
    val isTrackingEnabled: Boolean = true,
    val warningEnabled: Boolean = true
)
