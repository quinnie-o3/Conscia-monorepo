package com.example.conscia.ui.dashboard

import com.example.conscia.domain.model.TrackedAppLimitInfo
import com.example.conscia.model.AppUsageInfo
import com.example.conscia.model.DailyUsagePoint

data class DashboardUiState(
    val hasUsagePermission: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val totalTodayUsageMillis: Long = 0L,
    val trackedTodayUsageMillis: Long = 0L,
    val otherTodayUsageMillis: Long = 0L,
    val todayTopApps: List<AppUsageInfo> = emptyList(),
    val trackedAppStatuses: List<TrackedAppLimitInfo> = emptyList(),
    val weeklyTotalUsageMillis: Long = 0L,
    val weeklySummaryLabel: String = "",
    val hasLockedWeeklySummary: Boolean = false,
    val weeklyPreview: List<DailyUsagePoint> = emptyList(),
    val exceededCount: Int = 0,
    val nearLimitCount: Int = 0,
    val isEmpty: Boolean = false
)
