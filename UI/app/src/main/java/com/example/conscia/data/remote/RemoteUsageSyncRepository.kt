package com.example.conscia.data.remote

import android.content.Context
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.InsightResponse
import com.example.conscia.data.remote.dto.SyncSessionPayload
import com.example.conscia.data.remote.dto.SyncSessionsBatchRequest
import com.example.conscia.data.remote.dto.SyncSessionsResult
import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.data.usage.UsagePermissionHelper
import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.model.DailyAppUsageSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteUsageSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageRepository: UsageStatsRepository,
    private val ruleRepository: RuleRepository,
    private val dataStore: TrackedAppsDataStore,
    private val apiService: ConsciaApiService
) {
    suspend fun syncRecentUsage(days: Int = DEFAULT_SYNC_DAYS): SyncSessionsResult = withContext(Dispatchers.IO) {
        if (!UsagePermissionHelper.isUsageAccessGranted(context)) {
            return@withContext SyncSessionsResult()
        }

        val snapshots = usageRepository.getDailyUsageSnapshots(days)
        val rulesByPackage = ruleRepository.allRules.first().associateBy { it.packageName }
        val deviceId = resolveDeviceId()
        val todayStartMillis = startOfTodayMillis()
        val todayLocalDate = formatLocalDate(todayStartMillis)
        val syncedTodayPackages = snapshots
            .asSequence()
            .filter { it.localDate == todayLocalDate }
            .map { it.packageName }
            .toSet()
        val sessions = snapshots.map { snapshot ->
            buildSessionPayload(
                snapshot = snapshot,
                deviceId = deviceId,
                rule = rulesByPackage[snapshot.packageName]
            )
        } + rulesByPackage.values
            .filter { it.trackingEnabled && it.packageName !in syncedTodayPackages }
            .map { rule ->
                buildZeroUsagePayload(
                    rule = rule,
                    deviceId = deviceId,
                    localDate = todayLocalDate,
                    dayStartMillis = todayStartMillis
                )
            }

        if (sessions.isEmpty()) {
            return@withContext SyncSessionsResult()
        }

        val payload = SyncSessionsBatchRequest(
            deviceId = deviceId,
            sessions = sessions
        )

        val response = apiService.syncSessions(payload)
        val body = response.body()

        if (!response.isSuccessful || body == null || !body.success || body.data == null) {
            throw IllegalStateException(body?.message ?: "Sync request failed")
        }

        body.data
    }

    suspend fun fetchPurposeInsights(
        rangeStartMillis: Long,
        rangeEndMillis: Long
    ): InsightResponse = withContext(Dispatchers.IO) {
        val deviceId = resolveDeviceId()
        val response = apiService.getUsageByPurpose(
            deviceId = deviceId,
            from = formatLocalDate(rangeStartMillis),
            to = formatLocalDate(rangeEndMillis),
            timezone = TimeZone.getDefault().id
        )
        val body = response.body()

        if (!response.isSuccessful || body == null || !body.success || body.data == null) {
            throw IllegalStateException(body?.message ?: "Insights request failed")
        }

        body.data
    }

    private suspend fun resolveDeviceId(): String {
        return dataStore.deviceIdFlow.firstOrNull() ?: dataStore.generateAndSaveDeviceId()
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

    private fun buildZeroUsagePayload(
        rule: RuleEntity,
        deviceId: String,
        localDate: String,
        dayStartMillis: Long
    ): SyncSessionPayload {
        val timezone = TimeZone.getDefault()
        val intentionLabel = rule.intentionLabel
            .takeIf { rule.trackingEnabled && it.isNotBlank() }
            ?.trim()
        val dayStartIso = formatIsoUtc(dayStartMillis)

        return SyncSessionPayload(
            externalId = "$deviceId:${rule.packageName}:$localDate",
            deviceId = deviceId,
            packageName = rule.packageName,
            appName = rule.appName,
            startedAt = dayStartIso,
            endedAt = dayStartIso,
            durationSeconds = 0L,
            deviceLocalDate = localDate,
            deviceTimezone = timezone.id,
            timezoneOffsetMinutes = timezone.getOffset(dayStartMillis) / (60 * 1000),
            intentionLabel = intentionLabel,
            trackingEnabled = rule.trackingEnabled,
            warningEnabled = rule.warningEnabled,
            dailyLimitMinutes = rule.dailyLimitMinutes,
            tags = intentionLabel?.let { listOf(it) } ?: emptyList(),
            isClassified = intentionLabel != null
        )
    }

    private fun startOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
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
