package com.example.conscia.monitoring

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.example.conscia.data.remote.RemoteUsageSyncRepository
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

    @Inject
    lateinit var remoteUsageSyncRepository: RemoteUsageSyncRepository

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private var promptPackageName: String? = null
    private var foregroundPackageName: String? = null
    private var foregroundSessionStartedAt: Long = 0L
    private var foregroundSessionStartUsageMillis: Long = 0L
    private var limitMonitorJob: Job? = null
    private var promptResolutionJob: Job? = null
    private var promptResolutionPackageName: String? = null
    private var usageSyncJob: Job? = null
    private var lastUsageSyncElapsed: Long = 0L

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
            val isBlockedForToday = warningHistoryStore.isBlockedForToday(packageName)

            withContext(Dispatchers.Main) {
                startOrUpdateForegroundSession(packageName, usageStatsMillis)
                val currentUsageMillis = currentRealtimeUsageMillis(packageName, usageStatsMillis)
                if (isBlockedForToday || currentUsageMillis >= effectiveLimitMillis) {
                    promptPackageName = null
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

                    if (!PurposeGateStore.isAllowedForCurrentSession(this@AccessibilityForegroundAppService, packageName)) {
                        if (promptPackageName != packageName) {
                            promptPackageName = packageName
                            launchIntentionPrompt(
                                packageName = activeRule.packageName,
                                appName = activeRule.appName,
                                ruleId = activeRule.id,
                                intentionLabel = activeRule.intentionLabel
                            )
                            notificationManager.showIntentionPromptNotification(
                                packageName = activeRule.packageName,
                                appName = activeRule.appName,
                                ruleId = activeRule.id,
                                intentionLabel = activeRule.intentionLabel
                            )
                        }
                    } else {
                        notificationManager.cancelIntentionPromptNotification(packageName)
                        promptPackageName = null
                    }
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
                        val isBlockedForToday = warningHistoryStore.isBlockedForToday(packageName)

                        withContext(Dispatchers.Main) {
                            promptPackageName = null
                            notificationManager.cancelIntentionPromptNotification(packageName)
                            startOrUpdateForegroundSession(packageName, usageStatsMillis)
                            val currentUsageMillis = currentRealtimeUsageMillis(packageName, usageStatsMillis)
                            if (isBlockedForToday || currentUsageMillis >= effectiveLimitMillis) {
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
        intentionLabel: String
    ) {
        val intent = Intent(this, IntentionPromptActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(IntentionPromptActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(IntentionPromptActivity.EXTRA_APP_NAME, appName)
            putExtra(IntentionPromptActivity.EXTRA_RULE_ID, ruleId)
            putExtra(IntentionPromptActivity.EXTRA_INTENTION_LABEL, intentionLabel)
        }
        startActivity(intent)
    }

    private fun startOrUpdateForegroundSession(packageName: String, usageStatsMillis: Long) {
        if (foregroundPackageName != packageName) {
            if (foregroundPackageName != null) {
                scheduleRecentUsageSync()
            }
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

    private fun stopForegroundSession(forceUsageSync: Boolean = false) {
        if (foregroundPackageName != null) {
            scheduleRecentUsageSync(force = forceUsageSync)
        }
        foregroundPackageName = null
        foregroundSessionStartedAt = 0L
        foregroundSessionStartUsageMillis = 0L
        limitMonitorJob?.cancel()
        limitMonitorJob = null
    }

    private fun scheduleRecentUsageSync(force: Boolean = false) {
        val now = SystemClock.elapsedRealtime()
        if (!force && now - lastUsageSyncElapsed < USAGE_SYNC_THROTTLE_MS) return
        if (usageSyncJob?.isActive == true) return

        lastUsageSyncElapsed = now
        usageSyncJob = serviceScope.launch {
            runCatching {
                remoteUsageSyncRepository.syncRecentUsage(days = 1)
            }
        }
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
                scheduleRecentUsageSync()
                val isBlockedForToday = warningHistoryStore.isBlockedForToday(packageName)
                if (isBlockedForToday || currentUsageMillis >= effectiveLimitMillis) {
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
        serviceScope.launch {
            warningHistoryStore.markBlockedForToday(packageName)
            if (shouldNotify) {
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
        promptPackageName = null
        stopForegroundSession(forceUsageSync = true)

        val usageText = TimeFormatters.formatDurationShort(usageMillis)
        val limitText = TimeFormatters.formatDurationShort(limitMillis)
        val warningLaunched = launchLimitWarning(
            appName = appName,
            usageText = usageText,
            limitText = limitText
        )
        if (!warningLaunched) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun launchLimitWarning(appName: String, usageText: String, limitText: String): Boolean {
        val intent = Intent(this, UsageLimitWarningActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(UsageLimitWarningActivity.EXTRA_APP_NAME, appName)
            putExtra(UsageLimitWarningActivity.EXTRA_USAGE_TEXT, usageText)
            putExtra(UsageLimitWarningActivity.EXTRA_LIMIT_TEXT, limitText)
        }
        return runCatching {
            startActivity(intent)
        }.isSuccess
    }

    override fun onInterrupt() = Unit

    private companion object {
        const val USAGE_SYNC_THROTTLE_MS = 30_000L
    }
}
