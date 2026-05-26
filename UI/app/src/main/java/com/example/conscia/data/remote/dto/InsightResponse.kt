package com.example.conscia.data.remote.dto

data class InsightResponse(
    val range: InsightRange = InsightRange(),
    val summary: InsightSummary = InsightSummary(),
    val details: List<InsightDetail> = emptyList(),
    val apps: List<InsightAppUsage> = emptyList(),
    val topTrackedApp: TopTrackedApp? = null
)

data class InsightRange(
    val from: String = "",
    val to: String = "",
    val dayCount: Int = 0
)

data class InsightSummary(
    val totalSeconds: Long = 0L,
    val purposefulPercentage: Double = 0.0,
    val distractingPercentage: Double = 0.0,
    val trackedSeconds: Long = 0L,
    val otherSeconds: Long = 0L,
    val trackedAppsCount: Int = 0,
    val averageDailySeconds: Long = 0L
)

data class InsightDetail(
    val tagName: String = "",
    val duration: Long = 0L,
    val percentage: Double = 0.0,
    val colorCode: String = "#999999",
    val category: String = "OTHER"
)

data class InsightAppUsage(
    val packageName: String = "",
    val appName: String = "",
    val totalDurationSeconds: Long = 0L,
    val percentage: Double = 0.0
)

data class TopTrackedApp(
    val packageName: String = "",
    val appName: String = "",
    val totalDurationSeconds: Long = 0L
)
