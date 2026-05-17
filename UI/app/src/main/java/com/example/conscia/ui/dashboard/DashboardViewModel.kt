package com.example.conscia.ui.dashboard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsagePermissionHelper
import com.example.conscia.data.weekly.WeeklySummaryManager
import com.example.conscia.domain.model.UsageLimitStatus
import com.example.conscia.domain.usecase.EvaluateTrackedAppsUsageUseCase
import com.example.conscia.domain.usecase.GetRulesUseCase
import com.example.conscia.domain.usecase.GetTodayUsageUseCase
import com.example.conscia.domain.usecase.GetWeeklyUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val application: Application,
    private val getTodayUsageUseCase: GetTodayUsageUseCase,
    private val getWeeklyUsageUseCase: GetWeeklyUsageUseCase,
    private val getRulesUseCase: GetRulesUseCase,
    private val weeklySummaryManager: WeeklySummaryManager,
    private val evaluateUseCase: EvaluateTrackedAppsUsageUseCase,
    private val ruleRepository: RuleRepository,
    private val apiService: ConsciaApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        refresh()
        loadUserProfile()
    }

    fun refresh() {
        val hasPermission = UsagePermissionHelper.isUsageAccessGranted(application)
        _uiState.update { it.copy(hasUsagePermission = hasPermission) }
        
        if (hasPermission) {
            loadDashboardData()
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val response = apiService.getUserProfile()
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    _uiState.update { it.copy(
                        userName = user?.displayName ?: "User"
                    ) }
                }
            } catch (e: Exception) {
                // Keep default Guest
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val todayUsage = getTodayUsageUseCase()
                val totalToday = todayUsage.sumOf { it.totalTimeInForegroundMillis }
                
                val weeklyBreakdown = getWeeklyUsageUseCase()
                
                val allRules = getRulesUseCase().first()
                val trackedPackages = allRules.filter { it.trackingEnabled }.map { it.packageName }.toSet()
                
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

    fun extendLimit(ruleId: Long) {
        viewModelScope.launch {
            val rule = ruleRepository.getRuleById(ruleId) ?: return@launch
            val today = dateFormatter.format(Date())
            
            val currentCount = if (rule.lastExtensionDate == today) rule.extensionCount else 0
            if (currentCount >= 3) {
                _uiState.update { it.copy(errorMessage = "Maximum 3 extensions reached for ${rule.appName} today") }
                return@launch
            }

            val updatedRule = if (rule.lastExtensionDate == today) {
                rule.copy(
                    extensionMinutes = rule.extensionMinutes + 5,
                    extensionCount = rule.extensionCount + 1
                )
            } else {
                rule.copy(
                    extensionMinutes = 5, 
                    extensionCount = 1,
                    lastExtensionDate = today
                )
            }
            
            ruleRepository.updateRule(updatedRule)
            refresh()
        }
    }

    fun onGrantUsageAccessClicked() {
        UsagePermissionHelper.openUsageAccessSettings(application)
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
