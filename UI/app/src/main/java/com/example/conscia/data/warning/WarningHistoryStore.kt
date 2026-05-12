package com.example.conscia.data.warning

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.warningDataStore by preferencesDataStore(name = "warning_history")

class WarningHistoryStore(private val context: Context) {

    private fun getTodayDateKey(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}_${calendar.get(Calendar.DAY_OF_YEAR)}"
    }

    suspend fun wasExceededWarningSentToday(packageName: String): Boolean {
        val key = longPreferencesKey("exceeded_${packageName}_${getTodayDateKey()}")
        return context.warningDataStore.data.map { it[key] != null }.first()
    }

    suspend fun markExceededWarningSent(packageName: String) {
        val key = longPreferencesKey("exceeded_${packageName}_${getTodayDateKey()}")
        context.warningDataStore.edit { it[key] = System.currentTimeMillis() }
    }

    suspend fun wasNearLimitWarningSentToday(packageName: String): Boolean {
        val key = longPreferencesKey("near_${packageName}_${getTodayDateKey()}")
        return context.warningDataStore.data.map { it[key] != null }.first()
    }

    suspend fun markNearLimitWarningSent(packageName: String) {
        val key = longPreferencesKey("near_${packageName}_${getTodayDateKey()}")
        context.warningDataStore.edit { it[key] = System.currentTimeMillis() }
    }
}
