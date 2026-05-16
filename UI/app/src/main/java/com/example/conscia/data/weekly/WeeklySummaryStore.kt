package com.example.conscia.data.weekly

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.weeklySummaryDataStore by preferencesDataStore(name = "weekly_summary")

data class WeeklySummarySnapshot(
    val anchorMillis: Long?,
    val rangeStartMillis: Long,
    val rangeEndMillis: Long,
    val totalUsageMillis: Long,
    val isLockedSnapshot: Boolean
)

data class StoredWeeklySummary(
    val anchorMillis: Long,
    val rangeStartMillis: Long,
    val rangeEndMillis: Long,
    val totalUsageMillis: Long
)

@Singleton
class WeeklySummaryStore @Inject constructor(@ApplicationContext private val context: Context) {
    private companion object {
        val ANCHOR_KEY = longPreferencesKey("anchor_millis")
        val RANGE_START_KEY = longPreferencesKey("range_start_millis")
        val RANGE_END_KEY = longPreferencesKey("range_end_millis")
        val TOTAL_USAGE_KEY = longPreferencesKey("total_usage_millis")
    }

    suspend fun readSummary(): StoredWeeklySummary? {
        return context.weeklySummaryDataStore.data
            .map { preferences ->
                val anchorMillis = preferences[ANCHOR_KEY] ?: return@map null
                val rangeStartMillis = preferences[RANGE_START_KEY] ?: return@map null
                val rangeEndMillis = preferences[RANGE_END_KEY] ?: return@map null
                val totalUsageMillis = preferences[TOTAL_USAGE_KEY] ?: return@map null

                StoredWeeklySummary(
                    anchorMillis = anchorMillis,
                    rangeStartMillis = rangeStartMillis,
                    rangeEndMillis = rangeEndMillis,
                    totalUsageMillis = totalUsageMillis
                )
            }
            .first()
    }

    suspend fun saveSummary(summary: StoredWeeklySummary) {
        context.weeklySummaryDataStore.edit { preferences ->
            preferences[ANCHOR_KEY] = summary.anchorMillis
            preferences[RANGE_START_KEY] = summary.rangeStartMillis
            preferences[RANGE_END_KEY] = summary.rangeEndMillis
            preferences[TOTAL_USAGE_KEY] = summary.totalUsageMillis
        }
    }
}
