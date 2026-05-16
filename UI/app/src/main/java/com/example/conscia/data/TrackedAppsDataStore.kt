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
    val deviceId: String? = null,
    val accessToken: String? = null,
    val userEmail: String? = null,
    val lastUsedEmail: String? = null
)

class TrackedAppsDataStore(private val context: Context) {
    companion object {
        val SELECTED_PACKAGES_KEY = stringSetPreferencesKey("selected_packages")
        val IS_ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("is_onboarding_completed")
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        val DEVICE_ID_KEY = stringPreferencesKey("device_id")
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val LAST_USED_EMAIL_KEY = stringPreferencesKey("last_used_email")
    }

    val appPreferencesFlow: Flow<AppPreferencesState> = context.dataStore.data
        .map { preferences ->
            AppPreferencesState(
                selectedPackages = preferences[SELECTED_PACKAGES_KEY] ?: emptySet(),
                isOnboardingCompleted = preferences[IS_ONBOARDING_COMPLETED_KEY] ?: false,
                isDarkMode = preferences[IS_DARK_MODE_KEY] ?: false,
                deviceId = preferences[DEVICE_ID_KEY],
                accessToken = preferences[ACCESS_TOKEN_KEY],
                userEmail = preferences[USER_EMAIL_KEY],
                lastUsedEmail = preferences[LAST_USED_EMAIL_KEY]
            )
        }

    val accessTokenFlow: Flow<String?> = appPreferencesFlow.map { it.accessToken }
    val selectedPackagesFlow: Flow<Set<String>> = appPreferencesFlow.map { it.selectedPackages }
    val isDarkModeFlow: Flow<Boolean> = appPreferencesFlow.map { it.isDarkMode }
    val lastUsedEmailFlow: Flow<String?> = appPreferencesFlow.map { it.lastUsedEmail }

    suspend fun saveAuthToken(token: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
            preferences[USER_EMAIL_KEY] = email
            preferences[LAST_USED_EMAIL_KEY] = email // Remember for next time
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(USER_EMAIL_KEY)
            // LAST_USED_EMAIL_KEY is NOT removed to remember it for suggestions
        }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = enabled
        }
    }

    val isOnboardingCompletedFlow: Flow<Boolean> = appPreferencesFlow.map { it.isOnboardingCompleted }
    val deviceIdFlow: Flow<String?> = appPreferencesFlow.map { it.deviceId }

    suspend fun saveSelectedPackages(packages: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_PACKAGES_KEY] = packages
        }
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences -> preferences[IS_ONBOARDING_COMPLETED_KEY] = completed }
    }

    suspend fun generateAndSaveDeviceId(): String {
        var resolvedDeviceId: String? = null
        context.dataStore.edit { preferences ->
            val deviceId = preferences[DEVICE_ID_KEY] ?: UUID.randomUUID().toString()
            preferences[DEVICE_ID_KEY] = deviceId
            resolvedDeviceId = deviceId
        }
        return checkNotNull(resolvedDeviceId)
    }
}
