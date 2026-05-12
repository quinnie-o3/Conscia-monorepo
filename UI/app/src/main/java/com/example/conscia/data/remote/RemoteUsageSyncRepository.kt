package com.example.conscia.data.remote

import android.content.Context
import com.example.conscia.data.AppDatabase
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.remote.dto.InsightResponse
import com.example.conscia.data.remote.dto.SyncSessionPayload
import com.example.conscia.data.remote.dto.SyncSessionsBatchRequest
import com.example.conscia.data.remote.dto.SyncSessionsResult
import com.example.conscia.data.remote.network.RetrofitClient
import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsagePermissionHelper
import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.model.DailyAppUsageSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RemoteUsageSyncRepository(private val context: Context) {
    private val usageRepository = UsageStatsRepository(context)
    private val ruleRepository = RuleRepository(AppDatabase.getDatabase(context).ruleDao())
    private val dataStore = TrackedAppsDataStore(context)
    private val apiService = RetrofitClient.getApiService()

    suspend fun syncRecentUsage(days: Int = DEFAULT_SYNC_DAYS): SyncSessionsResult = withContext(Dispatchers.IO) {
        if (!UsagePermissionHelper.isUsageAccessGranted(context)) {
            return@withContext SyncSessionsResult()
        }

        val snapshots = usageRepository.getDailyUsageSnapshots(days)
        if (snapshots.isEmpty()) {
            return@withContext SyncSessionsResult()
        }

        val rulesByPackage = ruleRepository.allRules.first().associateBy { it.packageName }
        val deviceId = resolveDeviceId()
        val payload = SyncSessionsBatchRequest(
            sessions = snapshots.map { snapshot ->
                buildSessionPayload(
                    snapshot = snapshot,
                    deviceId = deviceId,
                    rule = rulesByPackage[snapshot.packageName]
                )
            }
        )

        val response = apiService.syncSessions(payload).execute()
        val body = response.body()

        if (!response.isSuccessful || body == null || !body.success || body.data == null) {
            throw IllegalStateException(body?.error ?: body?.message ?: "Sync request failed")
        }

        body.data
    }

    suspend fun fetchPurposeInsights(
        rangeStartMillis: Long,
        rangeEndMillis: Long
    ): InsightResponse = withContext(Dispatchers.IO) {
        val deviceId = resolveDeviceId()
        val response = apiService.getPurposeInsights(
            deviceId,
            formatLocalDate(rangeStartMillis),
            formatLocalDate(rangeEndMillis)
        ).execute()
        val body = response.body()

        if (!response.isSuccessful || body == null || !body.success || body.data == null) {
            throw IllegalStateException(body?.error ?: body?.message ?: "Insights request failed")
        }

        body.data
    }

    private suspend fun resolveDeviceId(): String {
        return dataStore.deviceIdFlow.first() ?: dataStore.generateAndSaveDeviceId()
    }

    private fun buildSessionPayload(
        snapshot: DailyAppUsageSnapshot,
        deviceId: String,
        rule: RuleEntity?
    ): SyncSessionPayload {
        val timezone = TimeZone.getDefault()
        val trackingEnabled = rule?.trackingEnabled == true
        val intentionLabel = rule?.intentionLabel
            ?.takeIf { trackingEnabled && it.isNotBlank() }
            ?.trim()
        val safeDurationMillis = snapshot.totalTimeInForegroundMillis.coerceAtLeast(1L)
        val endedAtMillis = snapshot.lastTimeUsed
            .takeIf { it in snapshot.dayStartMillis..snapshot.dayEndMillis }
            ?: snapshot.dayEndMillis
        val startedAtMillis = (endedAtMillis - safeDurationMillis).coerceAtLeast(snapshot.dayStartMillis)

        return SyncSessionPayload(
            externalId = "$deviceId:${snapshot.packageName}:${snapshot.localDate}",
            deviceId = deviceId,
            packageName = snapshot.packageName,
            appName = snapshot.appName,
            startedAt = formatIsoUtc(startedAtMillis),
            endedAt = formatIsoUtc(maxOf(startedAtMillis + 1000L, endedAtMillis)),
            durationSeconds = ((safeDurationMillis + 999L) / 1000L).coerceAtLeast(1L),
            deviceLocalDate = snapshot.localDate,
            deviceTimezone = timezone.id,
            timezoneOffsetMinutes = timezone.getOffset(snapshot.dayStartMillis) / (60 * 1000),
            intentionLabel = intentionLabel,
            trackingEnabled = trackingEnabled,
            warningEnabled = rule?.warningEnabled ?: false,
            dailyLimitMinutes = rule?.dailyLimitMinutes,
            tags = intentionLabel?.let { listOf(it) } ?: emptyList(),
            isClassified = intentionLabel != null
        )
    }

    private fun formatLocalDate(timeMillis: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }.format(Date(timeMillis))
    }

    private fun formatIsoUtc(timeMillis: Long): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timeMillis))
    }

    private companion object {
        const val DEFAULT_SYNC_DAYS = 28
    }
}
