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
import com.example.conscia.presentation.warning.UsageLimitWarningActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsciaNotificationManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        const val CHANNEL_ID = "usage_limit_warning"
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Usage limit warnings"
            val descriptionText = "Alerts when tracked apps approach or exceed their usage limits"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showExceededNotification(appName: String, usageStr: String, limitStr: String, packageName: String) {
        val intent = Intent(context, UsageLimitWarningActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
            .setContentIntent(pendingIntent)
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
