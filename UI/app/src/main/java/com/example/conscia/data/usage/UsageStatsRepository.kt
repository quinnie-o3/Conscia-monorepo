package com.example.conscia.data.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.example.conscia.model.AppUsageInfo
import com.example.conscia.model.DailyAppUsageSnapshot
import com.example.conscia.model.DailyUsagePoint
import com.example.conscia.util.DateRangeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.text.SimpleDateFormat

class UsageStatsRepository(private val context: Context) {
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

    suspend fun getWeeklyUsageBreakdown(): List<DailyUsagePoint> = withContext(Dispatchers.IO) {
        val result = mutableListOf<DailyUsagePoint>()
        val calendar = Calendar.getInstance()
        
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
            
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                endOfDay
            )
            
            val totalMillis = stats?.sumOf { it.totalTimeInForeground } ?: 0L
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
            val localDate = dateFormatter.format(Date(startOfDay))
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                endOfDay
            )

            if (stats.isNullOrEmpty()) continue

            stats
                .filter { it.totalTimeInForeground > 0 }
                .groupBy { it.packageName }
                .forEach { (packageName, entries) ->
                    val totalMillis = entries.sumOf { it.totalTimeInForeground }
                    val lastTimeUsed = entries.maxOfOrNull { it.lastTimeUsed } ?: startOfDay
                    val appName = try {
                        val appInfo = packageManager.getApplicationInfo(packageName, 0)
                        packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        packageName
                    }

                    if (packageName != context.packageName) {
                        snapshots.add(
                            DailyAppUsageSnapshot(
                                packageName = packageName,
                                appName = appName,
                                totalTimeInForegroundMillis = totalMillis,
                                lastTimeUsed = lastTimeUsed,
                                dayStartMillis = startOfDay,
                                dayEndMillis = endOfDay,
                                localDate = localDate
                            )
                        )
                    }
                }
        }

        snapshots.sortedWith(
            compareByDescending<DailyAppUsageSnapshot> { it.localDate }
                .thenByDescending { it.totalTimeInForegroundMillis }
        )
    }

    private suspend fun getUsageForPeriod(startTime: Long, endTime: Long): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (stats.isNullOrEmpty()) return@withContext emptyList<AppUsageInfo>()

        stats.filter { it.totalTimeInForeground > 0 }
            .map { usageStats ->
                val packageName = usageStats.packageName
                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    packageName
                }
                
                AppUsageInfo(
                    packageName = packageName,
                    appName = appName,
                    totalTimeInForegroundMillis = usageStats.totalTimeInForeground,
                    lastTimeUsed = usageStats.lastTimeUsed
                )
            }
            .filter { it.packageName != context.packageName } // Exclude own app
            .groupBy { it.packageName }
            .map { (pkg, list) ->
                list.reduce { acc, info ->
                    acc.copy(totalTimeInForegroundMillis = acc.totalTimeInForegroundMillis + info.totalTimeInForegroundMillis)
                }
            }
            .sortedByDescending { it.totalTimeInForegroundMillis }
    }

    suspend fun getTopUsedApps(limit: Int = 5): List<AppUsageInfo> {
        return getTodayUsage().take(limit)
    }
}
