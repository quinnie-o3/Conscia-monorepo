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

    private var lastPackageName: String? = null
    private var lastTriggerTime: Long = 0
    private val cooldownMs = 5_000L // 10 giây kiểm tra lại một lần khi đang dùng app

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        
        // Bỏ qua nếu là chính ứng dụng Conscia hoặc hệ thống
        if (packageName == this.packageName || packageName == "com.android.systemui" || packageName == "android") return

        serviceScope.launch {
            val rules = ruleRepository.allRules.first()
            val activeRule = rules.find { it.packageName == packageName && it.trackingEnabled }
                ?: return@launch

            val currentUsageMillis = usageRepository
                .getTodayUsage()
                .find { it.packageName == packageName }
                ?.totalTimeInForegroundMillis
                ?: 0L

            // Tính toán giới hạn (Gốc + Gia hạn)
            val today = dateFormatter.format(Date())
            val extensionMins = if (activeRule.lastExtensionDate == today) activeRule.extensionMinutes else 0
            val effectiveLimitMinutes = activeRule.dailyLimitMinutes + extensionMins
            val effectiveLimitMillis = effectiveLimitMinutes.toLong() * 60 * 1000

            withContext(Dispatchers.Main) {
                if (currentUsageMillis >= effectiveLimitMillis && activeRule.warningEnabled) {
                    // 1. TRƯỜNG HỢP VƯỢT GIỚI HẠN: Chặn app ngay
                    launchUsageLimitWarning(activeRule.appName)
                } else if (packageName != lastPackageName) {
                    // 2. TRƯỜNG HỢP MỚI MỞ APP (Chưa vượt hạn): Hỏi mục đích sử dụng
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTriggerTime > 30_000L) { // Cooldown 30s cho prompt
                        lastTriggerTime = currentTime
                        launchIntentionPrompt(activeRule.packageName, activeRule.appName, activeRule.id)
                    }
                }
            }
            lastPackageName = packageName
        }
    }

    private fun launchIntentionPrompt(packageName: String, appName: String, ruleId: Long) {
        val intent = Intent(this, IntentionPromptActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("EXTRA_PACKAGE_NAME", packageName)
            putExtra("EXTRA_APP_NAME", appName)
            putExtra("EXTRA_RULE_ID", ruleId)
        }
        startActivity(intent)
    }

    private fun launchUsageLimitWarning(appName: String) {
        val intent = Intent(this, UsageLimitWarningActivity::class.java).apply {
            // Quan trọng: Sử dụng NEW_TASK và CLEAR_TASK để đè lên app hiện tại hoàn toàn
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(UsageLimitWarningActivity.EXTRA_APP_NAME, appName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() = Unit
}
