package com.example.conscia.monitoring

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.conscia.data.AppDatabase
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.presentation.intervention.IntentionPromptActivity
import com.example.conscia.presentation.warning.UsageLimitWarningActivity
import com.example.conscia.util.TimeFormatters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccessibilityForegroundAppService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var ruleRepository: RuleRepository
    private lateinit var usageRepository: UsageStatsRepository

    private var lastPackageName: String? = null
    private var lastTriggerTime: Long = 0
    private val cooldownMs = 30_000L

    override fun onCreate() {
        super.onCreate()
        ruleRepository = RuleRepository(AppDatabase.getDatabase(this).ruleDao())
        usageRepository = UsageStatsRepository(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName || packageName == "com.android.systemui") return
        if (packageName == lastPackageName) return

        lastPackageName = packageName

        serviceScope.launch {
            val activeRule = ruleRepository
                .allRules
                .first()
                .firstOrNull { it.packageName == packageName && it.trackingEnabled }
                ?: return@launch

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTriggerTime <= cooldownMs) return@launch

            val currentUsageMillis = usageRepository
                .getTodayUsage()
                .firstOrNull { it.packageName == packageName }
                ?.totalTimeInForegroundMillis
                ?: 0L
            val dailyLimitMillis = activeRule.dailyLimitMinutes.toLong() * 60 * 1000

            lastTriggerTime = currentTime
            withContext(Dispatchers.Main) {
                if (activeRule.warningEnabled && dailyLimitMillis > 0 && currentUsageMillis >= dailyLimitMillis) {
                    launchUsageLimitWarning(
                        appName = activeRule.appName,
                        usageText = TimeFormatters.formatDurationShort(currentUsageMillis),
                        limitText = TimeFormatters.formatDurationDailyLimit(activeRule.dailyLimitMinutes)
                    )
                } else {
                    launchIntentionPrompt(
                        packageName = activeRule.packageName,
                        appName = activeRule.appName,
                        ruleId = activeRule.id
                    )
                }
            }
        }
    }

    private fun launchIntentionPrompt(packageName: String, appName: String, ruleId: Long) {
        val intent = Intent(this, IntentionPromptActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("EXTRA_PACKAGE_NAME", packageName)
            putExtra("EXTRA_APP_NAME", appName)
            putExtra("EXTRA_RULE_ID", ruleId)
        }
        startActivity(intent)
    }

    private fun launchUsageLimitWarning(appName: String, usageText: String, limitText: String) {
        val intent = Intent(this, UsageLimitWarningActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(UsageLimitWarningActivity.EXTRA_APP_NAME, appName)
            putExtra(UsageLimitWarningActivity.EXTRA_USAGE_TEXT, usageText)
            putExtra(UsageLimitWarningActivity.EXTRA_LIMIT_TEXT, limitText)
        }
        startActivity(intent)
    }

    override fun onInterrupt() = Unit
}
