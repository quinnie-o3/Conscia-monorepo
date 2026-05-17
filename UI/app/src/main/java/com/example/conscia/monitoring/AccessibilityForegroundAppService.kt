package com.example.conscia.monitoring

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.presentation.intervention.IntentionPromptActivity
import com.example.conscia.presentation.warning.UsageLimitWarningActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
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

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private var promptPackageName: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName == this.packageName) {
            if (promptPackageName == null) {
                PurposeGateStore.clear(this)
            }
            return
        }

        if (packageName == "com.android.systemui" || packageName == "android") {
            return
        }

        PurposeGateStore.clearIfDifferentPackage(this, packageName)

        serviceScope.launch {
            val rules = ruleRepository.allRules.first()
            val activeRule = rules.find { it.packageName == packageName && it.trackingEnabled }
                ?: run {
                    promptPackageName = null
                    return@launch
                }

            val currentUsageMillis = usageRepository
                .getTodayUsage()
                .find { it.packageName == packageName }
                ?.totalTimeInForegroundMillis
                ?: 0L

            val today = dateFormatter.format(Date())
            val extensionMins = if (activeRule.lastExtensionDate == today) activeRule.extensionMinutes else 0
            val effectiveLimitMinutes = activeRule.dailyLimitMinutes + extensionMins
            val effectiveLimitMillis = effectiveLimitMinutes.toLong() * 60 * 1000

            withContext(Dispatchers.Main) {
                if (currentUsageMillis >= effectiveLimitMillis && activeRule.warningEnabled) {
                    promptPackageName = null
                    launchUsageLimitWarning(activeRule.appName)
                } else if (
                    !PurposeGateStore.isAllowedForCurrentSession(this@AccessibilityForegroundAppService, packageName) &&
                    promptPackageName != packageName
                ) {
                    promptPackageName = packageName
                    launchIntentionPrompt(
                        packageName = activeRule.packageName,
                        appName = activeRule.appName,
                        ruleId = activeRule.id,
                        intentionLabel = activeRule.intentionLabel
                    )
                } else if (PurposeGateStore.isAllowedForCurrentSession(this@AccessibilityForegroundAppService, packageName)) {
                    promptPackageName = null
                }
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

    private fun launchUsageLimitWarning(appName: String) {
        val intent = Intent(this, UsageLimitWarningActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(UsageLimitWarningActivity.EXTRA_APP_NAME, appName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() = Unit
}
