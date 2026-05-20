package com.example.conscia.data.usage

import android.app.usage.UsageStatsManager
import android.app.usage.UsageEvents
import android.content.Context
import com.example.conscia.model.AppUsageInfo
import com.example.conscia.model.DailyAppUsageSnapshot
import com.example.conscia.model.DailyUsagePoint
import com.example.conscia.util.DateRangeUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.text.SimpleDateFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    suspend fun getTodayUsage(): List<AppUsageInfo> = getUsageForPeriod(
        startTime = DateRangeUtils.getStartOfTodayMillis(),
        endTime = System.currentTimeMillis()
    )

    suspend fun getUsageForLastNDays(days: Int): List<AppUsageInfo> = getUsageForPeriod(
        startTime = DateRangeUtils.getStartOfDaysAgo(days),
        endTime = System.currentTimeMillis()
    )

    suspend fun getUsageBetween(startTime: Long, endTime: Long): List<AppUsageInfo> = getUsageForPeriod(
        startTime = startTime,
        endTime = endTime
    )

    suspend fun getWeeklyUsageBreakdown(packageNames: Set<String> = emptySet()): List<DailyUsagePoint> = withContext(Dispatchers.IO) {
        val result = mutableListOf<DailyUsagePoint>()
        
        for (i in 0..6) {
            val dayCalendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = dayCalendar.timeInMillis
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1
            val usageEndMillis = minOf(endOfDay, System.currentTimeMillis())

            val totalMillis = collectUsageFromEvents(startOfDay, usageEndMillis)
                .values
                .filter { packageNames.isEmpty() || it.packageName in packageNames }
                .sumOf { it.totalTimeInForegroundMillis }
            result.add(DailyUsagePoint(startOfDay, totalMillis))
        }
        result.sortedBy { it.dayStartMillis }
    }

    suspend fun getDailyUsageSnapshots(days: Int): List<DailyAppUsageSnapshot> = withContext(Dispatchers.IO) {
        if (days <= 0) return@withContext emptyList()

        val snapshots = mutableListOf<DailyAppUsageSnapshot>()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }

        for (dayOffset in 0 until days) {
            val dayCalendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -dayOffset)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val startOfDay = dayCalendar.timeInMillis
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1
            val usageEndMillis = minOf(endOfDay, System.currentTimeMillis())
            val localDate = dateFormatter.format(Date(startOfDay))
            collectUsageFromEvents(startOfDay, usageEndMillis)
                .values
                .filter { it.totalTimeInForegroundMillis > 0 }
                .forEach { usage ->
                    snapshots.add(
                        DailyAppUsageSnapshot(
                            packageName = usage.packageName,
                            appName = usage.appName,
                            totalTimeInForegroundMillis = usage.totalTimeInForegroundMillis,
                            lastTimeUsed = usage.lastTimeUsed,
                            dayStartMillis = startOfDay,
                            dayEndMillis = endOfDay,
                            localDate = localDate
                        )
                    )
                }
        }

        snapshots.sortedWith(
            compareByDescending<DailyAppUsageSnapshot> { it.localDate }
                .thenByDescending { it.totalTimeInForegroundMillis }
        )
    }

    private suspend fun getUsageForPeriod(startTime: Long, endTime: Long): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        collectUsageFromEvents(startTime, endTime)
            .values
            .sortedByDescending { it.totalTimeInForegroundMillis }
    }

    private fun collectUsageFromEvents(startTime: Long, endTime: Long): Map<String, AppUsageInfo> {
        if (endTime <= startTime) return emptyMap()

        val usageByPackage = mutableMapOf<String, UsageAccumulator>()
        val activeStarts = mutableMapOf<String, Long>()
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName ?: continue
            if (packageName == context.packageName) continue

            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    activeStarts.putIfAbsent(packageName, event.timeStamp.coerceAtLeast(startTime))
                    usageByPackage.getOrPut(packageName) {
                        UsageAccumulator(
                            packageName = packageName,
                            appName = resolveAppName(packageName)
                        )
                    }.launchCount += 1
                }

                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val foregroundStartedAt = activeStarts.remove(packageName) ?: continue
                    val endedAt = event.timeStamp.coerceIn(startTime, endTime)
                    val durationMillis = (endedAt - foregroundStartedAt).coerceAtLeast(0L)
                    if (durationMillis > 0L) {
                        usageByPackage.getOrPut(packageName) {
                            UsageAccumulator(
                                packageName = packageName,
                                appName = resolveAppName(packageName)
                            )
                        }.apply {
                            totalTimeInForegroundMillis += durationMillis
                            lastTimeUsed = maxOf(lastTimeUsed, endedAt)
                        }
                    }
                }
            }
        }

        activeStarts.forEach { (packageName, foregroundStartedAt) ->
            val durationMillis = (endTime - foregroundStartedAt).coerceAtLeast(0L)
            if (durationMillis > 0L) {
                usageByPackage.getOrPut(packageName) {
                    UsageAccumulator(
                        packageName = packageName,
                        appName = resolveAppName(packageName)
                    )
                }.apply {
                    totalTimeInForegroundMillis += durationMillis
                    lastTimeUsed = maxOf(lastTimeUsed, endTime)
                }
            }
        }

        return usageByPackage
            .filterValues { it.totalTimeInForegroundMillis > 0L }
            .mapValues { (_, usage) ->
                AppUsageInfo(
                    packageName = usage.packageName,
                    appName = usage.appName,
                    totalTimeInForegroundMillis = usage.totalTimeInForegroundMillis,
                    lastTimeUsed = usage.lastTimeUsed,
                    launchCount = usage.launchCount
                )
            }
    }

    suspend fun getTopUsedApps(limit: Int = 5): List<AppUsageInfo> {
        return getTodayUsage().take(limit)
    }

    private fun resolveAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private data class UsageAccumulator(
        val packageName: String,
        val appName: String,
        var totalTimeInForegroundMillis: Long = 0L,
        var lastTimeUsed: Long = 0L,
        var launchCount: Int = 0
    )
}
