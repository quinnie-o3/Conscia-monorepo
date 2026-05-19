package com.example.conscia.domain.model

data class TrackedAppLimitInfo(
    val ruleId: Long,
    val packageName: String,
    val appName: String,
    val intentionLabel: String,
    val todayUsageMillis: Long,
    val todayLaunchCount: Int,
    val dailyLimitMinutes: Int,
    val dailyLimitMillis: Long,
    val remainingMillis: Long,
    val exceededMillis: Long,
    val usagePercent: Float,
    val status: UsageLimitStatus,
    val trackingEnabled: Boolean,
    val warningEnabled: Boolean,
    val extensionCount: Int = 0,
    val canExtend: Boolean = true
)
