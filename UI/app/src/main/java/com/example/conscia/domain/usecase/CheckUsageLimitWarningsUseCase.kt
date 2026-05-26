package com.example.conscia.domain.usecase

import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.data.warning.WarningHistoryStore
import com.example.conscia.domain.model.UsageLimitStatus
import com.example.conscia.notification.ConsciaNotificationManager
import com.example.conscia.util.TimeFormatters
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CheckUsageLimitWarningsUseCase @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val usageRepository: UsageStatsRepository,
    private val warningHistoryStore: WarningHistoryStore,
    private val notificationManager: ConsciaNotificationManager,
    private val evaluateUseCase: EvaluateTrackedAppsUsageUseCase
) {
    suspend fun execute() {
        val rules = ruleRepository.allRules.first()
        val activeRules = rules.filter { it.trackingEnabled }
        if (activeRules.isEmpty()) return

        val todayUsage = usageRepository.getTodayUsage()
        val trackedStatuses = evaluateUseCase.execute(activeRules, todayUsage)

        trackedStatuses.forEach { info ->
            val packageName = info.packageName
            val appName = info.appName
            val usageStr = TimeFormatters.formatDurationShort(info.todayUsageMillis)
            val limitStr = TimeFormatters.formatDurationDailyLimit(info.dailyLimitMinutes)

            when (info.status) {
                UsageLimitStatus.EXCEEDED -> {
                    warningHistoryStore.markBlockedForToday(packageName)
                    if (info.warningEnabled && !warningHistoryStore.wasExceededWarningSentToday(packageName)) {
                        notificationManager.showExceededNotification(appName, usageStr, limitStr, packageName)
                        warningHistoryStore.markExceededWarningSent(packageName)
                    }
                }
                UsageLimitStatus.NEAR_LIMIT -> {
                    if (info.warningEnabled &&
                        !warningHistoryStore.wasNearLimitWarningSentToday(packageName) &&
                        !warningHistoryStore.wasExceededWarningSentToday(packageName)) {
                        notificationManager.showNearLimitNotification(appName, usageStr, limitStr, packageName)
                        warningHistoryStore.markNearLimitWarningSent(packageName)
                    }
                }
                else -> {}
            }
        }
    }
}
