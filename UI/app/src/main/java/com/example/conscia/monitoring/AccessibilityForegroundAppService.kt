package com.example.conscia.monitoring

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.data.warning.WarningHistoryStore
import com.example.conscia.notification.ConsciaNotificationManager
import com.example.conscia.presentation.intervention.IntentionPromptActivity
import com.example.conscia.presentation.warning.UsageLimitWarningActivity
import com.example.conscia.util.TimeFormatters
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityForegroundAppService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var ruleRepository: RuleRepository

    @Inject
    lateinit var usageRepository: UsageStatsRepository

    @Inject
    lateinit var notificationManager: ConsciaNotificationManager

    @Inject
    lateinit var warningHistoryStore: WarningHistoryStore

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private var promptPackageName: String? = null
    private var foregroundPackageName: String? = null
    private var foregroundSessionStartedAt: Long = 0L
    private var foregroundSessionStartUsageMillis: Long = 0L
    private var limitMonitorJob: Job? = null
    private var promptResolutionJob: Job? = null
    private var promptResolutionPackageName: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName == this.packageName) {
            val pendingPromptPackageName = promptPackageName
            if (pendingPromptPackageName == null) {
                PurposeGateStore.clear(this)
            } else {
                startPromptResolutionMonitor(pendingPromptPackageName)
            }
            stopForegroundSession()
            return
        }

        if (packageName == "com.android.systemui" || packageName == "android") {
            if (promptPackageName == null) {
                PurposeGateStore.clear(this)
            }
            stopForegroundSession()
            return
        }

        PurposeGateStore.clearIfDifferentPackage(this, packageName)

        serviceScope.launch {
            val rules = ruleRepository.allRules.first()
            val activeRule = rules.find { it.packageName == packageName && it.trackingEnabled }
                ?: run {
                    promptPackageName = null
                    withContext(Dispatchers.Main) {
                        stopForegroundSession()
                    }
                    return@launch
                }

            val usageStatsMillis = usageRepository
                .getTodayUsage()
                .find { it.packageName == packageName }
                ?.totalTimeInForegroundMillis
                ?: 0L

            val today = dateFormatter.format(Date())
            val extensionMins = if (activeRule.lastExtensionDate == today) activeRule.extensionMinutes else 0
            val effectiveLimitMinutes = activeRule.dailyLimitMinutes + extensionMins
            val effectiveLimitMillis = effectiveLimitMinutes.toLong() * 60 * 1000

            withContext(Dispatchers.Main) {
                startOrUpdateForegroundSession(packageName, usageStatsMillis)
                val currentUsageMillis = currentRealtimeUsageMillis(packageName, usageStatsMillis)
                if (currentUsageMillis >= effectiveLimitMillis) {
                    promptPackageName = null
                    handleLimitReached(
                        packageName = activeRule.packageName,
                        appName = activeRule.appName,
                        usageMillis = currentUsageMillis,
                        limitMillis = effectiveLimitMillis,
                        shouldNotify = activeRule.warningEnabled
                    )
                } else if (
                    !PurposeGateStore.isAllowedForCurrentSession(this@AccessibilityForegroundAppService, packageName) &&
                    promptPackageName != packageName
                ) {
                    promptPackageName = packageName
                    launchIntentionPrompt(
                        packageName = activeRule.packageName,
                        appName = activeRule.appName,
                        ruleId = activeRule.id,
                        intentionLabel = activeRule.intentionLabel,
                        otherIntentionLabels = rules
                            .map { it.intentionLabel }
                            .filter { it.isNotBlank() && it != activeRule.intentionLabel }
                            .distinct()
                            .take(3)
                    )
                } else if (PurposeGateStore.isAllowedForCurrentSession(this@AccessibilityForegroundAppService, packageName)) {
                    promptPackageName = null
                    startLimitMonitor(
                        packageName = activeRule.packageName,
                        appName = activeRule.appName,
                        effectiveLimitMillis = effectiveLimitMillis,
                        shouldNotify = activeRule.warningEnabled
                    )
                }
            }
        }
    }

    private fun startPromptResolutionMonitor(packageName: String) {
        if (promptResolutionJob?.isActive == true && promptResolutionPackageName == packageName) return
        promptResolutionJob?.cancel()
        promptResolutionPackageName = packageName
        promptResolutionJob = serviceScope.launch {
            try {
                repeat(30) {
                    delay(500L)
                    if (PurposeGateStore.isAllowedForCurrentSession(this@AccessibilityForegroundAppService, packageName)) {
                        val rules = ruleRepository.allRules.first()
                        val activeRule = rules.find { it.packageName == packageName && it.trackingEnabled }
                            ?: return@launch
                        val usageStatsMillis = usageRepository
                            .getTodayUsage()
                            .find { it.packageName == packageName }
                            ?.totalTimeInForegroundMillis
                            ?: 0L

                        val today = dateFormatter.format(Date())
                        val extensionMins = if (activeRule.lastExtensionDate == today) {
                            activeRule.extensionMinutes
                        } else {
                            0
                        }
                        val effectiveLimitMillis = (activeRule.dailyLimitMinutes + extensionMins).toLong() * 60 * 1000

                        withContext(Dispatchers.Main) {
                            promptPackageName = null
                            startOrUpdateForegroundSession(packageName, usageStatsMillis)
                            val currentUsageMillis = currentRealtimeUsageMillis(packageName, usageStatsMillis)
                            if (currentUsageMillis >= effectiveLimitMillis) {
                                handleLimitReached(
                                    packageName = activeRule.packageName,
                                    appName = activeRule.appName,
                                    usageMillis = currentUsageMillis,
                                    limitMillis = effectiveLimitMillis,
                                    shouldNotify = activeRule.warningEnabled
                                )
                            } else {
                                startLimitMonitor(
                                    packageName = activeRule.packageName,
                                    appName = activeRule.appName,
                                    effectiveLimitMillis = effectiveLimitMillis,
                                    shouldNotify = activeRule.warningEnabled
                                )
                            }
                        }
                        return@launch
                    }
                }
                if (promptPackageName == packageName) {
                    promptPackageName = null
                }
            } finally {
                promptResolutionPackageName = null
            }
        }
    }

    private fun launchIntentionPrompt(
        packageName: String,
        appName: String,
        ruleId: Long,
        intentionLabel: String,
        otherIntentionLabels: List<String>
    ) {
        val intent = Intent(this, IntentionPromptActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(IntentionPromptActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(IntentionPromptActivity.EXTRA_APP_NAME, appName)
            putExtra(IntentionPromptActivity.EXTRA_RULE_ID, ruleId)
            putExtra(IntentionPromptActivity.EXTRA_INTENTION_LABEL, intentionLabel)
            putStringArrayListExtra(
                IntentionPromptActivity.EXTRA_OTHER_INTENTION_LABELS,
                ArrayList(otherIntentionLabels)
            )
        }
        startActivity(intent)
    }

    private fun startOrUpdateForegroundSession(packageName: String, usageStatsMillis: Long) {
        if (foregroundPackageName != packageName) {
            foregroundPackageName = packageName
            foregroundSessionStartedAt = SystemClock.elapsedRealtime()
            foregroundSessionStartUsageMillis = usageStatsMillis
            limitMonitorJob?.cancel()
            limitMonitorJob = null
        } else if (usageStatsMillis > foregroundSessionStartUsageMillis) {
            val elapsedMillis = SystemClock.elapsedRealtime() - foregroundSessionStartedAt
            foregroundSessionStartUsageMillis = usageStatsMillis - elapsedMillis.coerceAtLeast(0L)
        }
    }

    private fun currentRealtimeUsageMillis(packageName: String, usageStatsMillis: Long): Long {
        if (foregroundPackageName != packageName) return usageStatsMillis
        val elapsedMillis = SystemClock.elapsedRealtime() - foregroundSessionStartedAt
        return maxOf(usageStatsMillis, foregroundSessionStartUsageMillis + elapsedMillis.coerceAtLeast(0L))
    }

    private fun stopForegroundSession() {
        foregroundPackageName = null
        foregroundSessionStartedAt = 0L
        foregroundSessionStartUsageMillis = 0L
        limitMonitorJob?.cancel()
        limitMonitorJob = null
    }

    private fun startLimitMonitor(
        packageName: String,
        appName: String,
        effectiveLimitMillis: Long,
        shouldNotify: Boolean
    ) {
        if (limitMonitorJob?.isActive == true) return
        limitMonitorJob = serviceScope.launch {
            while (isActive && foregroundPackageName == packageName) {
                delay(1_000L)
                val usageStatsMillis = usageRepository
                    .getTodayUsage()
                    .find { it.packageName == packageName }
                    ?.totalTimeInForegroundMillis
                    ?: 0L
                val currentUsageMillis = currentRealtimeUsageMillis(packageName, usageStatsMillis)
                if (currentUsageMillis >= effectiveLimitMillis) {
                    withContext(Dispatchers.Main) {
                        handleLimitReached(
                            packageName = packageName,
                            appName = appName,
                            usageMillis = currentUsageMillis,
                            limitMillis = effectiveLimitMillis,
                            shouldNotify = shouldNotify
                        )
                    }
                    break
                }
            }
        }
    }

    private fun handleLimitReached(
        packageName: String,
        appName: String,
        usageMillis: Long,
        limitMillis: Long,
        shouldNotify: Boolean
    ) {
        if (shouldNotify) {
            serviceScope.launch {
                if (!warningHistoryStore.wasExceededWarningSentToday(packageName)) {
                    notificationManager.showExceededNotification(
                        appName = appName,
                        usageStr = TimeFormatters.formatDurationShort(usageMillis),
                        limitStr = TimeFormatters.formatDurationShort(limitMillis),
                        packageName = packageName
                    )
                    warningHistoryStore.markExceededWarningSent(packageName)
                }
            }
        }
        PurposeGateStore.clear(this)
        stopForegroundSession()
        performGlobalAction(GLOBAL_ACTION_HOME)
        serviceScope.launch {
            delay(250L)
            withContext(Dispatchers.Main) {
                launchLimitWarning(appName)
            }
        }
    }

    private fun launchLimitWarning(appName: String) {
        val intent = Intent(this, UsageLimitWarningActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(UsageLimitWarningActivity.EXTRA_APP_NAME, appName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() = Unit
}
