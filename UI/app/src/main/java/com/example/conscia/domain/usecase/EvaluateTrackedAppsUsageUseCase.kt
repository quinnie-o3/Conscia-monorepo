package com.example.conscia.domain.usecase

import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.domain.model.TrackedAppLimitInfo
import com.example.conscia.domain.model.UsageLimitStatus
import com.example.conscia.model.AppUsageInfo
import kotlin.math.max

class EvaluateTrackedAppsUsageUseCase {
    
    fun execute(
        rules: List<RuleEntity>,
        usageStats: List<AppUsageInfo>
    ): List<TrackedAppLimitInfo> {
        return rules
            .filter { it.trackingEnabled }
            .map { rule ->
                val usage = usageStats.find { it.packageName == rule.packageName }
                val todayUsageMillis = usage?.totalTimeInForegroundMillis ?: 0L
                val dailyLimitMillis = rule.dailyLimitMinutes.toLong() * 60 * 1000
                
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
                    dailyLimitMinutes = rule.dailyLimitMinutes,
                    dailyLimitMillis = dailyLimitMillis,
                    remainingMillis = max(0L, dailyLimitMillis - todayUsageMillis),
                    exceededMillis = max(0L, todayUsageMillis - dailyLimitMillis),
                    usagePercent = usagePercent,
                    status = status,
                    trackingEnabled = rule.trackingEnabled,
                    warningEnabled = rule.warningEnabled
                )
            }
            .sortedWith(
                compareByDescending<TrackedAppLimitInfo> { it.status == UsageLimitStatus.EXCEEDED }
                    .thenByDescending { it.status == UsageLimitStatus.NEAR_LIMIT }
                    .thenByDescending { it.usagePercent }
            )
    }
}
