package com.example.conscia.util

import java.util.concurrent.TimeUnit

object TimeFormatters {
    fun formatDurationShort(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = (TimeUnit.MILLISECONDS.toMinutes(millis) % 60)
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "0m"
        }
    }

    fun formatDurationDailyLimit(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m/day"
            h > 0 -> "${h}h/day"
            else -> "${m}m/day"
        }
    }
}
