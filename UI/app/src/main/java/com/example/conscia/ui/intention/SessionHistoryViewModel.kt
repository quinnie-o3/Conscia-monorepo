package com.example.conscia.ui.intention

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.remote.RemoteUsageSyncRepository
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.AppUsageStats
import com.example.conscia.data.TrackedAppsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SessionHistoryUiState(
    val isLoading: Boolean = false,
    val usageStats: List<AppUsageStats> = emptyList(),
    val totalFocusMinutes: Int = 0, // Thay đổi sang Focus Minutes
    val errorMessage: String? = null
)

@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    private val apiService: ConsciaApiService,
    private val remoteUsageSyncRepository: RemoteUsageSyncRepository,
    private val dataStore: TrackedAppsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionHistoryUiState())
    val uiState: StateFlow<SessionHistoryUiState> = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        refresh(showLoading = true)
    }

    fun refresh(showLoading: Boolean = false) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            try {
                runCatching { remoteUsageSyncRepository.syncRecentUsage() }
                val deviceId = dataStore.deviceIdFlow.firstOrNull() ?: ""
                val today = dateFormatter.format(Date())
                val response = apiService.getDailyStats(deviceId, today)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    val sortedUsageStats = data?.byApp
                        ?.sortedWith(
                            compareByDescending<AppUsageStats> { it.durationSeconds }
                                .thenBy { it.appName.lowercase(Locale.US) }
                        )
                        ?: emptyList()
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            usageStats = sortedUsageStats,
                            totalFocusMinutes = data?.totalTrackedMinutes ?: 0, // Lấy từ field totalTrackedMinutes của Backend
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load history") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}

private fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
    while (true) {
        val prevValue = value
        val nextValue = function(prevValue)
        if (compareAndSet(prevValue, nextValue)) {
            return
        }
    }
}
