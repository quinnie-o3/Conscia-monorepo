package com.example.conscia.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.AppDatabase
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsagePermissionHelper
import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.data.weekly.WeeklySummaryManager
import com.example.conscia.domain.model.UsageLimitStatus
import com.example.conscia.domain.usecase.EvaluateTrackedAppsUsageUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val usageRepository = UsageStatsRepository(application)
    private val ruleRepository = RuleRepository(AppDatabase.getDatabase(application).ruleDao())
    private val weeklySummaryManager = WeeklySummaryManager(application)
    private val evaluateUseCase = EvaluateTrackedAppsUsageUseCase()
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val hasPermission = UsagePermissionHelper.isUsageAccessGranted(getApplication())
        _uiState.update { it.copy(hasUsagePermission = hasPermission) }
        
        if (hasPermission) {
            loadDashboardData()
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Get real usage data
                val todayUsage = usageRepository.getTodayUsage()
                val totalToday = todayUsage.sumOf { it.totalTimeInForegroundMillis }
                
                // 2. Get weekly preview
                val weeklyBreakdown = usageRepository.getWeeklyUsageBreakdown()
                
                // 3. Get rules
                val allRules = ruleRepository.allRules.first()
                val trackedPackages = allRules.filter { it.trackingEnabled }.map { it.packageName }.toSet()
                
                // 4. Evaluate limit status for tracked apps
                val trackedStatuses = evaluateUseCase.execute(allRules, todayUsage)
                val trackedTodayUsageMillis = todayUsage
                    .filter { it.packageName in trackedPackages }
                    .sumOf { it.totalTimeInForegroundMillis }
                val otherTodayUsageMillis = (totalToday - trackedTodayUsageMillis).coerceAtLeast(0L)
                val weeklySummary = weeklySummaryManager.getWeeklySummary()
                
                val exceeded = trackedStatuses.count { it.status == UsageLimitStatus.EXCEEDED }
                val nearLimit = trackedStatuses.count { it.status == UsageLimitStatus.NEAR_LIMIT }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        hasUsagePermission = true,
                        totalTodayUsageMillis = totalToday,
                        trackedTodayUsageMillis = trackedTodayUsageMillis,
                        otherTodayUsageMillis = otherTodayUsageMillis,
                        todayTopApps = todayUsage,
                        trackedAppStatuses = trackedStatuses,
                        weeklyTotalUsageMillis = weeklySummary.totalUsageMillis,
                        weeklySummaryLabel = buildWeeklySummaryLabel(
                            weeklySummary.rangeStartMillis,
                            weeklySummary.rangeEndMillis,
                            weeklySummary.isLockedSnapshot
                        ),
                        hasLockedWeeklySummary = weeklySummary.isLockedSnapshot,
                        weeklyPreview = weeklyBreakdown,
                        exceededCount = exceeded,
                        nearLimitCount = nearLimit,
                        isEmpty = todayUsage.isEmpty() && trackedStatuses.isEmpty(),
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to load usage data: ${e.message}"
                    )
                }
            }
        }
    }

    fun onGrantUsageAccessClicked() {
        UsagePermissionHelper.openUsageAccessSettings(getApplication())
    }

    private fun buildWeeklySummaryLabel(startMillis: Long, endMillis: Long, isLockedSnapshot: Boolean): String {
        val startLabel = weeklyRangeFormatter.format(Date(startMillis))
        val endLabel = weeklyRangeFormatter.format(Date(endMillis))
        return if (isLockedSnapshot) {
            "Weekly summary locked at Sunday 08:00 ($startLabel - $endLabel)"
        } else {
            "Live 7-day preview ($startLabel - $endLabel)"
        }
    }

    companion object {
        private val weeklyRangeFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
    }
}
