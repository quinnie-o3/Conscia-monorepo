package com.example.conscia.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tracked_apps_prefs")

data class AppPreferencesState(
    val selectedPackages: Set<String> = emptySet(),
    val isOnboardingCompleted: Boolean = false,
    val isDarkMode: Boolean = false,
    val deviceId: String? = null
)

class TrackedAppsDataStore(private val context: Context) {
    companion object {
        val SELECTED_PACKAGES_KEY = stringSetPreferencesKey("selected_packages")
        val IS_ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("is_onboarding_completed")
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    }

    val appPreferencesFlow: Flow<AppPreferencesState> = context.dataStore.data
        .map { preferences ->
            AppPreferencesState(
                selectedPackages = preferences[SELECTED_PACKAGES_KEY] ?: emptySet(),
                isOnboardingCompleted = preferences[IS_ONBOARDING_COMPLETED_KEY] ?: false,
                isDarkMode = preferences[IS_DARK_MODE_KEY] ?: false,
                deviceId = preferences[DEVICE_ID_KEY]
            )
        }

    val selectedPackagesFlow: Flow<Set<String>> = appPreferencesFlow
        .map { it.selectedPackages }

    val isOnboardingCompletedFlow: Flow<Boolean> = appPreferencesFlow
        .map { it.isOnboardingCompleted }

    val isDarkModeFlow: Flow<Boolean> = appPreferencesFlow
        .map { it.isDarkMode }

    val deviceIdFlow: Flow<String?> = appPreferencesFlow
        .map { it.deviceId }

    suspend fun saveSelectedPackages(packages: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_PACKAGES_KEY] = packages
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETED_KEY] = completed
        }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setDeviceId(deviceId: String) {
        context.dataStore.edit { preferences ->
            preferences[DEVICE_ID_KEY] = deviceId
        }
    }

    suspend fun generateAndSaveDeviceId(): String {
        var resolvedDeviceId: String? = null
        context.dataStore.edit { preferences ->
            val existingDeviceId = preferences[DEVICE_ID_KEY]
            val deviceId = existingDeviceId ?: UUID.randomUUID().toString()
            preferences[DEVICE_ID_KEY] = deviceId
            resolvedDeviceId = deviceId
        }
        return checkNotNull(resolvedDeviceId)
    }

    suspend fun completeOnboarding(): String {
        var resolvedDeviceId: String? = null
        context.dataStore.edit { preferences ->
            val existingDeviceId = preferences[DEVICE_ID_KEY]
            val deviceId = existingDeviceId ?: UUID.randomUUID().toString()
            preferences[DEVICE_ID_KEY] = deviceId
            preferences[IS_ONBOARDING_COMPLETED_KEY] = true
            resolvedDeviceId = deviceId
        }
        return checkNotNull(resolvedDeviceId)
    }
}
