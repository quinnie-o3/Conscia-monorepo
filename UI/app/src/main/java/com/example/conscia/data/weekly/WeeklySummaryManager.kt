package com.example.conscia.data.weekly

import android.content.Context
import com.example.conscia.data.usage.UsageStatsRepository
import java.util.Calendar

class WeeklySummaryManager(context: Context) {
    private val usageRepository = UsageStatsRepository(context)
    private val weeklySummaryStore = WeeklySummaryStore(context)

    suspend fun getWeeklySummary(nowMillis: Long = System.currentTimeMillis()): WeeklySummarySnapshot {
        refreshSnapshotIfNeeded(nowMillis)

        val storedSummary = weeklySummaryStore.readSummary()
        if (storedSummary != null) {
            return WeeklySummarySnapshot(
                anchorMillis = storedSummary.anchorMillis,
                rangeStartMillis = storedSummary.rangeStartMillis,
                rangeEndMillis = storedSummary.rangeEndMillis,
                totalUsageMillis = storedSummary.totalUsageMillis,
                isLockedSnapshot = true
            )
        }

        val liveRangeStart = nowMillis - sevenDaysMillis
        val liveUsage = usageRepository.getUsageBetween(liveRangeStart, nowMillis)
        return WeeklySummarySnapshot(
            anchorMillis = null,
            rangeStartMillis = liveRangeStart,
            rangeEndMillis = nowMillis,
            totalUsageMillis = liveUsage.sumOf { it.totalTimeInForegroundMillis },
            isLockedSnapshot = false
        )
    }

    suspend fun refreshSnapshotIfNeeded(nowMillis: Long = System.currentTimeMillis()) {
        val latestAnchorMillis = latestSundayEightAm(nowMillis) ?: return
        val storedSummary = weeklySummaryStore.readSummary()
        if (storedSummary?.anchorMillis == latestAnchorMillis) return

        val rangeStartMillis = latestAnchorMillis - sevenDaysMillis
        val usage = usageRepository.getUsageBetween(rangeStartMillis, latestAnchorMillis)
        val totalUsageMillis = usage.sumOf { it.totalTimeInForegroundMillis }

        weeklySummaryStore.saveSummary(
            StoredWeeklySummary(
                anchorMillis = latestAnchorMillis,
                rangeStartMillis = rangeStartMillis,
                rangeEndMillis = latestAnchorMillis,
                totalUsageMillis = totalUsageMillis
            )
        )
    }

    private fun latestSundayEightAm(nowMillis: Long): Long? {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        if (calendar.timeInMillis > nowMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, -7)
        }

        return calendar.timeInMillis
    }

    private companion object {
        const val sevenDaysMillis = 7L * 24L * 60L * 60L * 1000L
    }
}
