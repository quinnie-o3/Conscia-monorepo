package com.example.conscia.ui.dashboard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.remote.RemoteUsageSyncRepository
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsagePermissionHelper
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
    private val evaluateUseCase: EvaluateTrackedAppsUsageUseCase,
    private val ruleRepository: RuleRepository,
    private val remoteUsageSyncRepository: RemoteUsageSyncRepository,
    private val apiService: ConsciaApiService,
    private val dataStore: TrackedAppsDataStore
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        // Collect user info from DataStore for immediate display
        viewModelScope.launch {
            dataStore.userNameFlow.collect { name ->
                _uiState.update { it.copy(
                    userName = if (name.isNullOrBlank()) "User" else name
                ) }
            }
        }
        
        refresh()
        loadUserProfile() // Fetch latest from API
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
                    if (user != null) {
                        dataStore.updateUserInfo(user.displayName ?: "", "")
                    }
                }
            } catch (e: Exception) {
                // Ignore background fetch errors
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                ruleRepository.syncRulesFromServer()
                runCatching {
                    remoteUsageSyncRepository.syncRecentUsage(days = 1)
                }
                val todayUsage = getTodayUsageUseCase()
                val allRules = getRulesUseCase().first()
                val trackedPackages = allRules.filter { it.trackingEnabled }.map { it.packageName }.toSet()
                val hasTrackedRules = trackedPackages.isNotEmpty()
                val trackedTodayUsage = if (hasTrackedRules) {
                    todayUsage.filter { it.packageName in trackedPackages }
                } else {
                    emptyList()
                }
                val totalToday = trackedTodayUsage.sumOf { it.totalTimeInForegroundMillis }
                val weeklyBreakdown = if (hasTrackedRules) {
                    getWeeklyUsageUseCase(trackedPackages)
                } else {
                    emptyList()
                }

                val trackedStatuses = evaluateUseCase.execute(allRules, todayUsage)
                val weeklyTotalUsageMillis = weeklyBreakdown.sumOf { it.totalForegroundMillis }
                val exceeded = trackedStatuses.count { it.status == UsageLimitStatus.EXCEEDED }
                val nearLimit = trackedStatuses.count { it.status == UsageLimitStatus.NEAR_LIMIT }
                val now = System.currentTimeMillis()

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        hasUsagePermission = true,
                        totalTodayUsageMillis = totalToday,
                        trackedTodayUsageMillis = totalToday,
                        otherTodayUsageMillis = 0L,
                        hasTrackedRules = hasTrackedRules,
                        todayTopApps = trackedTodayUsage,
                        trackedAppStatuses = trackedStatuses,
                        weeklyTotalUsageMillis = weeklyTotalUsageMillis,
                        weeklySummaryLabel = if (hasTrackedRules) buildLiveWeeklySummaryLabel(now) else "",
                        hasLockedWeeklySummary = false,
                        weeklyPreview = weeklyBreakdown,
                        exceededCount = exceeded,
                        nearLimitCount = nearLimit,
                        isEmpty = !hasTrackedRules,
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
            try {
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
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to extend rule limit.")
                }
            }
        }
    }

    fun onGrantUsageAccessClicked() {
        UsagePermissionHelper.openUsageAccessSettings(application)
    }

    private fun buildLiveWeeklySummaryLabel(nowMillis: Long): String {
        val startMillis = nowMillis - sevenDaysMillis
        val endMillis = nowMillis
        val startLabel = weeklyRangeFormatter.format(Date(startMillis))
        val endLabel = weeklyRangeFormatter.format(Date(endMillis))
        return "Live 7-day preview for your rules ($startLabel - $endLabel)"
    }

    companion object {
        private val weeklyRangeFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
        private const val sevenDaysMillis = 7L * 24L * 60L * 60L * 1000L
    }
}
