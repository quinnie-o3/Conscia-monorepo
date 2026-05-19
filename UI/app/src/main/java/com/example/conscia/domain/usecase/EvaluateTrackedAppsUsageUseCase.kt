package com.example.conscia.domain.usecase

import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.domain.model.TrackedAppLimitInfo
import com.example.conscia.domain.model.UsageLimitStatus
import com.example.conscia.model.AppUsageInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

class EvaluateTrackedAppsUsageUseCase @Inject constructor() {
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun execute(
        rules: List<RuleEntity>,
        usageStats: List<AppUsageInfo>
    ): List<TrackedAppLimitInfo> {
        val today = dateFormatter.format(Date())

        return rules
            .filter { it.trackingEnabled }
            .map { rule ->
                val usage = usageStats.find { it.packageName == rule.packageName }
                val todayUsageMillis = usage?.totalTimeInForegroundMillis ?: 0L
                
                // Calculate effective limit: base limit + extensions (if extension is for today)
                val isExtensionForToday = rule.lastExtensionDate == today
                val extensionMins = if (isExtensionForToday) rule.extensionMinutes else 0
                val extensionCount = if (isExtensionForToday) rule.extensionCount else 0
                
                val effectiveLimitMinutes = rule.dailyLimitMinutes + extensionMins
                val dailyLimitMillis = effectiveLimitMinutes.toLong() * 60 * 1000
                
                val usagePercent = if (dailyLimitMillis > 0) {
                    todayUsageMillis.toFloat() / dailyLimitMillis
                } else {
                    0f
                }

                val status = when {
                    usagePercent >= 1.0f -> UsageLimitStatus.EXCEEDED
                    usagePercent >= 0.8f -> UsageLimitStatus.NEAR_LIMIT
                    else -> UsageLimitStatus.NORMAL
                }

                TrackedAppLimitInfo(
                    ruleId = rule.id,
                    packageName = rule.packageName,
                    appName = rule.appName,
                    intentionLabel = rule.intentionLabel,
                    todayUsageMillis = todayUsageMillis,
                    todayLaunchCount = usage?.launchCount ?: 0,
                    dailyLimitMinutes = effectiveLimitMinutes,
                    dailyLimitMillis = dailyLimitMillis,
                    remainingMillis = max(0L, dailyLimitMillis - todayUsageMillis),
                    exceededMillis = max(0L, todayUsageMillis - dailyLimitMillis),
                    usagePercent = usagePercent,
                    status = status,
                    trackingEnabled = rule.trackingEnabled,
                    warningEnabled = rule.warningEnabled,
                    extensionCount = extensionCount,
                    canExtend = extensionCount < 3
                )
            }
            .sortedWith(
                compareByDescending<TrackedAppLimitInfo> { it.status == UsageLimitStatus.EXCEEDED }
                    .thenByDescending { it.status == UsageLimitStatus.NEAR_LIMIT }
                    .thenByDescending { it.usagePercent }
            )
    }
}
