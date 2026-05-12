package com.example.conscia.domain.usecase

import android.content.Context
import com.example.conscia.data.AppDatabase
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.data.warning.WarningHistoryStore
import com.example.conscia.domain.model.UsageLimitStatus
import com.example.conscia.notification.ConsciaNotificationManager
import com.example.conscia.util.TimeFormatters
import kotlinx.coroutines.flow.first

class CheckUsageLimitWarningsUseCase(private val context: Context) {
    private val ruleRepository = RuleRepository(AppDatabase.getDatabase(context).ruleDao())
    private val usageRepository = UsageStatsRepository(context)
    private val warningHistoryStore = WarningHistoryStore(context)
    private val notificationManager = ConsciaNotificationManager(context)
    private val evaluateUseCase = EvaluateTrackedAppsUsageUseCase()

    suspend fun execute() {
        // 1. Load data
        val rules = ruleRepository.allRules.first()
        val activeRules = rules.filter { it.trackingEnabled && it.warningEnabled }
        if (activeRules.isEmpty()) return

        val todayUsage = usageRepository.getTodayUsage()
        
        // 2. Evaluate
        val trackedStatuses = evaluateUseCase.execute(activeRules, todayUsage)

        // 3. Dispatch
        trackedStatuses.forEach { info ->
            val packageName = info.packageName
            val appName = info.appName
            val usageStr = TimeFormatters.formatDurationShort(info.todayUsageMillis)
            val limitStr = TimeFormatters.formatDurationDailyLimit(info.dailyLimitMinutes)

            when (info.status) {
                UsageLimitStatus.EXCEEDED -> {
                    if (!warningHistoryStore.wasExceededWarningSentToday(packageName)) {
                        notificationManager.showExceededNotification(appName, usageStr, limitStr, packageName)
                        warningHistoryStore.markExceededWarningSent(packageName)
                    }
                }
                UsageLimitStatus.NEAR_LIMIT -> {
                    if (!warningHistoryStore.wasNearLimitWarningSentToday(packageName) && 
                        !warningHistoryStore.wasExceededWarningSentToday(packageName)) {
                        notificationManager.showNearLimitNotification(appName, usageStr, limitStr, packageName)
                        warningHistoryStore.markNearLimitWarningSent(packageName)
                    }
                }
                UsageLimitStatus.NORMAL -> {
                    // Do nothing
                }
            }
        }
    }
}
