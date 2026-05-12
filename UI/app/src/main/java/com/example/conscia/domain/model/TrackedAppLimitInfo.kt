package com.example.conscia.domain.model

data class TrackedAppLimitInfo(
    val ruleId: Long,
    val packageName: String,
    val appName: String,
    val intentionLabel: String,
    val todayUsageMillis: Long,
    val dailyLimitMinutes: Int,
    val dailyLimitMillis: Long,
    val remainingMillis: Long,
    val exceededMillis: Long,
    val usagePercent: Float,
    val status: UsageLimitStatus,
    val trackingEnabled: Boolean,
    val warningEnabled: Boolean
)
