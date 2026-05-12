package com.example.conscia.model

import android.graphics.drawable.Drawable

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForegroundMillis: Long,
    val lastTimeUsed: Long,
    val icon: Drawable? = null
)

data class DailyUsagePoint(
    val dayStartMillis: Long,
    val totalForegroundMillis: Long
)
