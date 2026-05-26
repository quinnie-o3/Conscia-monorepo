package com.example.conscia.ui.insights

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.remote.RemoteUsageSyncRepository
import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsagePermissionHelper
import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.util.TimeFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject
import kotlin.math.floor
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
    val appUsageRankings: List<AppUsageRanking> = emptyList(),
    val reflectionText: String = "",
    val lastUpdatedLabel: String = ""
)

data class AppUsageRanking(
    val packageName: String,
    val appName: String,
    val usageMillis: Long,
    val usagePercent: Int = 0,
    val limitMinutes: Int? = null
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val application: Application,
    private val usageRepository: UsageStatsRepository,
    private val ruleRepository: RuleRepository,
    private val remoteUsageSyncRepository: RemoteUsageSyncRepository
) : ViewModel() {

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
        UsagePermissionHelper.openUsageAccessSettings(application)
    }

    private fun loadInsightsForOffset(weekOffset: Int, showLoading: Boolean) {
        val range = buildSevenDayRange(weekOffset)
        val hasPermission = UsagePermissionHelper.isUsageAccessGranted(application)

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
                val trackedRules = ruleRepository.allRules.first().filter { it.trackingEnabled }
                val rulesByPackage = trackedRules.associateBy { it.packageName }
                val usageRankings = if (remoteInsights.apps.isNotEmpty()) {
                    buildAppUsageRankings(
                        usages = remoteInsights.apps.map { app ->
                            RawAppUsage(
                                packageName = app.packageName,
                                appName = app.appName,
                                usageMillis = app.totalDurationSeconds * 1000L
                            )
                        },
                        rulesByPackage = rulesByPackage
                    )
                } else {
                    buildAppUsageRankings(
                        usages = loadLocalUsageDurations(range),
                        rulesByPackage = rulesByPackage
                    )
                }

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
                        appUsageRankings = usageRankings,
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
        val rulesByPackage = trackedRules.associateBy { it.packageName }

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
                appUsageRankings = buildAppUsageRankings(
                    usages = usage.map { app ->
                        RawAppUsage(
                            packageName = app.packageName,
                            appName = app.appName,
                            usageMillis = app.totalTimeInForegroundMillis
                        )
                    },
                    rulesByPackage = rulesByPackage
                ),
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

    private suspend fun loadLocalUsageDurations(range: SevenDayRange): List<RawAppUsage> {
        return usageRepository.getUsageBetween(range.startMillis, range.endMillis)
            .map { app ->
                RawAppUsage(
                    packageName = app.packageName,
                    appName = app.appName,
                    usageMillis = app.totalTimeInForegroundMillis
                )
            }
    }

    private fun buildAppUsageRankings(
        usages: List<RawAppUsage>,
        rulesByPackage: Map<String, RuleEntity>
    ): List<AppUsageRanking> {
        val groupedUsage = usages
            .filter { it.usageMillis > 0L }
            .groupBy { it.packageName }
            .map { (packageName, entries) ->
                RawAppUsage(
                    packageName = packageName,
                    appName = entries.firstOrNull { it.appName.isNotBlank() }?.appName ?: packageName,
                    usageMillis = entries.sumOf { it.usageMillis }
                )
            }
            .sortedWith(
                compareByDescending<RawAppUsage> { it.usageMillis }
                    .thenBy { it.appName.lowercase(Locale.US) }
                    .thenBy { it.packageName }
            )
        val percentages = buildPercentageShares(groupedUsage.map { it.usageMillis })

        return groupedUsage.mapIndexed { index, app ->
            AppUsageRanking(
                packageName = app.packageName,
                appName = app.appName,
                usageMillis = app.usageMillis,
                usagePercent = percentages[index],
                limitMinutes = rulesByPackage[app.packageName]?.dailyLimitMinutes
            )
        }
    }

    private fun buildPercentageShares(values: List<Long>): List<Int> {
        val total = values.sum()
        if (total <= 0L) {
            return values.map { 0 }
        }

        val rawShares = values.map { value -> value.toDouble() * 100.0 / total.toDouble() }
        val wholeShares = rawShares.map { floor(it).toInt() }.toMutableList()
        val remainingPoints = 100 - wholeShares.sum()
        rawShares
            .mapIndexed { index, share -> index to (share - floor(share)) }
            .sortedByDescending { it.second }
            .take(remainingPoints)
            .forEach { (index, _) -> wholeShares[index] += 1 }

        return wholeShares
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
            trackedAppsCount == 0 -> "You spent ${TimeFormatters.formatDurationShort(totalUsageMillis)} in total, but no rules are configured yet."
            purposefulUsageMillis == 0L -> "You spent ${TimeFormatters.formatDurationShort(totalUsageMillis)} in total, but none of it matched your rules."
            !topTrackedAppName.isNullOrBlank() && topTrackedAppUsageMillis != null -> {
                "${TimeFormatters.formatDurationShort(purposefulUsageMillis)} of ${TimeFormatters.formatDurationShort(totalUsageMillis)} matched your rules. " +
                    "$topTrackedAppName led with ${TimeFormatters.formatDurationShort(topTrackedAppUsageMillis)}."
            }

            else -> "${TimeFormatters.formatDurationShort(purposefulUsageMillis)} of ${TimeFormatters.formatDurationShort(totalUsageMillis)} matched your rules in this range."
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

    private data class RawAppUsage(
        val packageName: String,
        val appName: String,
        val usageMillis: Long
    )

    companion object {
        private val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())
        private val dayFormatter = SimpleDateFormat("d", Locale.getDefault())
        private val shortDateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    }
}
