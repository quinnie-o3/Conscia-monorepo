package com.example.conscia.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.conscia.MainActivity
import com.example.conscia.R
import com.example.conscia.presentation.intervention.IntentionPromptActivity
import com.example.conscia.presentation.warning.UsageLimitWarningActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsciaNotificationManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        const val CHANNEL_ID = "usage_limit_warning"
        const val INTENTION_CHANNEL_ID = "intention_prompt"
        private const val INTENTION_NOTIFICATION_OFFSET = 10_000

        fun intentionNotificationId(packageName: String): Int {
            return packageName.hashCode() + INTENTION_NOTIFICATION_OFFSET
        }
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val warningChannel = NotificationChannel(
                CHANNEL_ID,
                "Usage limit warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when rules approach or exceed their usage limits"
            }
            val intentionChannel = NotificationChannel(
                INTENTION_CHANNEL_ID,
                "Reason prompts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Asks you to confirm why you are opening a tracked app"
            }

            notificationManager.createNotificationChannel(warningChannel)
            notificationManager.createNotificationChannel(intentionChannel)
        }
    }

    fun showIntentionPromptNotification(
        packageName: String,
        appName: String,
        ruleId: Long,
        intentionLabel: String
    ) {
        val intent = Intent(context, IntentionPromptActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(IntentionPromptActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(IntentionPromptActivity.EXTRA_APP_NAME, appName)
            putExtra(IntentionPromptActivity.EXTRA_RULE_ID, ruleId)
            putExtra(IntentionPromptActivity.EXTRA_INTENTION_LABEL, intentionLabel)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            intentionNotificationId(packageName),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, INTENTION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Confirm your reason")
            .setContentText("Before opening $appName, choose: $intentionLabel")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context)
                .notify(intentionNotificationId(packageName), builder.build())
        } catch (e: SecurityException) {
            // Notification permission missing.
        }
    }

    fun cancelIntentionPromptNotification(packageName: String) {
        NotificationManagerCompat.from(context)
            .cancel(intentionNotificationId(packageName))
    }

    fun showExceededNotification(appName: String, usageStr: String, limitStr: String, packageName: String) {
        val intent = Intent(context, UsageLimitWarningActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(UsageLimitWarningActivity.EXTRA_APP_NAME, appName)
            putExtra(UsageLimitWarningActivity.EXTRA_USAGE_TEXT, usageStr)
            putExtra(UsageLimitWarningActivity.EXTRA_LIMIT_TEXT, limitStr)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, packageName.hashCode(), intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Bạn đã chạm mức sử dụng")
            .setContentText("$appName đã dùng $usageStr hôm nay, vượt giới hạn $limitStr.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(packageName.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Permission missing
        }
    }

    fun showNearLimitNotification(appName: String, usageStr: String, limitStr: String, packageName: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, packageName.hashCode() + 1, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Sắp chạm giới hạn: $appName")
            .setContentText("$appName đã dùng $usageStr trên tổng giới hạn $limitStr.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(packageName.hashCode() + 1, builder.build())
        } catch (e: SecurityException) {
            // Permission missing
        }
    }
}
