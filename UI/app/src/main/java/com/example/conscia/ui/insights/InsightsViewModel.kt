package com.example.conscia.ui.insights

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.AppDatabase
import com.example.conscia.data.remote.RemoteUsageSyncRepository
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsagePermissionHelper
import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.util.TimeFormatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class InsightsUiState(
    val hasUsagePermission: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val weekOffset: Int = 0,
    val dateRangeLabel: String = "",
    val totalUsageMillis: Long = 0L,
    val purposefulUsageMillis: Long = 0L,
    val otherUsageMillis: Long = 0L,
    val averageDailyUsageMillis: Long = 0L,
    val purposefulPercent: Int = 0,
    val trackedAppsCount: Int = 0,
    val reflectionText: String = "",
    val lastUpdatedLabel: String = ""
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {
    private val usageRepository = UsageStatsRepository(application)
    private val ruleRepository = RuleRepository(AppDatabase.getDatabase(application).ruleDao())
    private val remoteUsageSyncRepository = RemoteUsageSyncRepository(application)

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        refresh(showLoading = true)
    }

    fun refresh(showLoading: Boolean = false) {
        loadInsightsForOffset(_uiState.value.weekOffset, showLoading)
    }

    fun showPreviousWeek() {
        val nextOffset = _uiState.value.weekOffset + 1
        _uiState.update { it.copy(weekOffset = nextOffset) }
        loadInsightsForOffset(nextOffset, showLoading = true)
    }

    fun showNextWeek() {
        val currentOffset = _uiState.value.weekOffset
        if (currentOffset == 0) return

        val nextOffset = currentOffset - 1
        _uiState.update { it.copy(weekOffset = nextOffset) }
        loadInsightsForOffset(nextOffset, showLoading = true)
    }

    fun onGrantUsageAccessClicked() {
        UsagePermissionHelper.openUsageAccessSettings(getApplication())
    }

    private fun loadInsightsForOffset(weekOffset: Int, showLoading: Boolean) {
        val range = buildSevenDayRange(weekOffset)
        val hasPermission = UsagePermissionHelper.isUsageAccessGranted(getApplication())

        if (!hasPermission) {
            _uiState.update {
                it.copy(
                    hasUsagePermission = false,
                    isLoading = false,
                    errorMessage = null,
                    dateRangeLabel = range.label,
                    reflectionText = "Grant usage access to load real activity for this range.",
                    lastUpdatedLabel = ""
                )
            }
            return
        }

        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            try {
                if (showLoading) {
                    remoteUsageSyncRepository.syncRecentUsage()
                }
                val remoteInsights = remoteUsageSyncRepository.fetchPurposeInsights(
                    rangeStartMillis = range.startMillis,
                    rangeEndMillis = range.endMillis
                )

                val totalUsageMillis = remoteInsights.summary.totalSeconds * 1000L
                val purposefulUsageMillis = remoteInsights.summary.trackedSeconds * 1000L
                val otherUsageMillis = remoteInsights.summary.otherSeconds * 1000L
                val averageDailyUsageMillis = remoteInsights.summary.averageDailySeconds * 1000L

                _uiState.update {
                    it.copy(
                        hasUsagePermission = true,
                        isLoading = false,
                        errorMessage = null,
                        dateRangeLabel = range.label,
                        totalUsageMillis = totalUsageMillis,
                        purposefulUsageMillis = purposefulUsageMillis,
                        otherUsageMillis = otherUsageMillis,
                        averageDailyUsageMillis = averageDailyUsageMillis,
                        purposefulPercent = remoteInsights.summary.purposefulPercentage.roundToInt(),
                        trackedAppsCount = remoteInsights.summary.trackedAppsCount,
                        reflectionText = buildReflection(
                            totalUsageMillis = totalUsageMillis,
                            purposefulUsageMillis = purposefulUsageMillis,
                            trackedAppsCount = remoteInsights.summary.trackedAppsCount,
                            topTrackedAppName = remoteInsights.topTrackedApp?.appName,
                            topTrackedAppUsageMillis = remoteInsights.topTrackedApp?.totalDurationSeconds?.times(1000L)
                        ),
                        lastUpdatedLabel = timeFormatter.format(Date())
                    )
                }
            } catch (remoteError: Exception) {
                try {
                    loadLocalInsights(
                        range = range,
                        fallbackMessage = "Showing local insights because backend sync is unavailable."
                    )
                } catch (localError: Exception) {
                    _uiState.update {
                        it.copy(
                            hasUsagePermission = true,
                            isLoading = false,
                            errorMessage = "Failed to load insights: ${localError.message}",
                            dateRangeLabel = range.label
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadLocalInsights(range: SevenDayRange, fallbackMessage: String?) {
        val usage = usageRepository.getUsageBetween(range.startMillis, range.endMillis)
        val trackedRules = ruleRepository.allRules.first().filter { it.trackingEnabled }
        val trackedPackages = trackedRules.map { it.packageName }.toSet()

        val totalUsageMillis = usage.sumOf { it.totalTimeInForegroundMillis }
        val purposefulUsage = usage
            .filter { it.packageName in trackedPackages }
            .sumOf { it.totalTimeInForegroundMillis }
        val otherUsage = (totalUsageMillis - purposefulUsage).coerceAtLeast(0L)
        val purposefulPercent = if (totalUsageMillis > 0) {
            (purposefulUsage.toDouble() * 100.0 / totalUsageMillis.toDouble()).roundToInt()
        } else {
            0
        }
        val averageDailyUsage = totalUsageMillis / 7
        val topTrackedApp = usage
            .filter { it.packageName in trackedPackages }
            .maxByOrNull { it.totalTimeInForegroundMillis }

        _uiState.update {
            it.copy(
                hasUsagePermission = true,
                isLoading = false,
                errorMessage = fallbackMessage,
                dateRangeLabel = range.label,
                totalUsageMillis = totalUsageMillis,
                purposefulUsageMillis = purposefulUsage,
                otherUsageMillis = otherUsage,
                averageDailyUsageMillis = averageDailyUsage,
                purposefulPercent = purposefulPercent,
                trackedAppsCount = trackedPackages.size,
                reflectionText = buildReflection(
                    totalUsageMillis = totalUsageMillis,
                    purposefulUsageMillis = purposefulUsage,
                    trackedAppsCount = trackedPackages.size,
                    topTrackedAppName = topTrackedApp?.appName,
                    topTrackedAppUsageMillis = topTrackedApp?.totalTimeInForegroundMillis
                ),
                lastUpdatedLabel = timeFormatter.format(Date())
            )
        }
    }

    private fun buildReflection(
        totalUsageMillis: Long,
        purposefulUsageMillis: Long,
        trackedAppsCount: Int,
        topTrackedAppName: String?,
        topTrackedAppUsageMillis: Long?
    ): String {
        return when {
            totalUsageMillis == 0L -> "No app activity was detected in this 7-day range yet."
            trackedAppsCount == 0 -> "You spent ${TimeFormatters.formatDurationShort(totalUsageMillis)} in total, but no tracked apps are configured yet."
            purposefulUsageMillis == 0L -> "You spent ${TimeFormatters.formatDurationShort(totalUsageMillis)} in total, but none of it matched your tracked apps."
            !topTrackedAppName.isNullOrBlank() && topTrackedAppUsageMillis != null -> {
                "${TimeFormatters.formatDurationShort(purposefulUsageMillis)} of ${TimeFormatters.formatDurationShort(totalUsageMillis)} was on tracked apps. " +
                    "$topTrackedAppName led with ${TimeFormatters.formatDurationShort(topTrackedAppUsageMillis)}."
            }

            else -> "${TimeFormatters.formatDurationShort(purposefulUsageMillis)} of ${TimeFormatters.formatDurationShort(totalUsageMillis)} was on tracked apps in this range."
        }
    }

    private fun buildSevenDayRange(weekOffset: Int): SevenDayRange {
        val endDayOffset = -(weekOffset * 7)
        val startDayOffset = endDayOffset - 6
        val startMillis = startOfDay(startDayOffset)
        val endDateMillis = startOfDay(endDayOffset)
        val endMillis = if (weekOffset == 0) {
            System.currentTimeMillis()
        } else {
            endOfDay(endDayOffset)
        }

        return SevenDayRange(
            startMillis = startMillis,
            endMillis = endMillis,
            label = formatRangeLabel(startMillis, endDateMillis)
        )
    }

    private fun startOfDay(dayOffset: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun endOfDay(dayOffset: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun formatRangeLabel(startMillis: Long, endMillis: Long): String {
        val startCalendar = Calendar.getInstance().apply { timeInMillis = startMillis }
        val endCalendar = Calendar.getInstance().apply { timeInMillis = endMillis }

        return if (
            startCalendar.get(Calendar.YEAR) == endCalendar.get(Calendar.YEAR) &&
            startCalendar.get(Calendar.MONTH) == endCalendar.get(Calendar.MONTH)
        ) {
            "${monthFormatter.format(Date(startMillis))} ${dayFormatter.format(Date(startMillis))}-${dayFormatter.format(Date(endMillis))}"
        } else {
            "${shortDateFormatter.format(Date(startMillis))} - ${shortDateFormatter.format(Date(endMillis))}"
        }
    }

    private data class SevenDayRange(
        val startMillis: Long,
        val endMillis: Long,
        val label: String
    )

    companion object {
        private val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())
        private val dayFormatter = SimpleDateFormat("d", Locale.getDefault())
        private val shortDateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    }
}
