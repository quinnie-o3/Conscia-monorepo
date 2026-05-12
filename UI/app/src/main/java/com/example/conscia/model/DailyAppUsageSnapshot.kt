package com.example.conscia.model

data class DailyAppUsageSnapshot(
    val packageName: String,
    val appName: String,
    val totalTimeInForegroundMillis: Long,
    val lastTimeUsed: Long,
    val dayStartMillis: Long,
    val dayEndMillis: Long,
    val localDate: String
)
