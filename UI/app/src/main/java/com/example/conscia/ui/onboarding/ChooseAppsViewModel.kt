package com.example.conscia.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.AppRepository
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.model.TrackedAppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChooseAppsUiState(
    val isLoading: Boolean = true,
    val installedApps: List<TrackedAppInfo> = emptyList(),
    val filteredApps: List<TrackedAppInfo> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val searchQuery: String = ""
)

@HiltViewModel
class ChooseAppsViewModel @Inject constructor(
    private val repository: AppRepository,
    private val dataStore: TrackedAppsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChooseAppsUiState())
    val uiState: StateFlow<ChooseAppsUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            val apps = repository.getInstalledApps()
            val savedPackages = dataStore.selectedPackagesFlow.first()
            
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    installedApps = apps,
                    filteredApps = filterApps(apps, state.searchQuery),
                    selectedPackages = savedPackages
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredApps = filterApps(state.installedApps, query)
            )
        }
    }

    fun toggleAppSelection(packageName: String) {
        _uiState.update { state ->
            val newSelection = if (state.selectedPackages.contains(packageName)) {
                state.selectedPackages - packageName
            } else {
                state.selectedPackages + packageName
            }
            state.copy(selectedPackages = newSelection)
        }
    }

    fun saveSelection(onComplete: () -> Unit) {
        viewModelScope.launch {
            dataStore.saveSelectedPackages(_uiState.value.selectedPackages)
            onComplete()
        }
    }

    fun skipSelection(onComplete: () -> Unit) {
        viewModelScope.launch {
            dataStore.saveSelectedPackages(emptySet())
            _uiState.update { it.copy(selectedPackages = emptySet()) }
            onComplete()
        }
    }

    private fun filterApps(apps: List<TrackedAppInfo>, query: String): List<TrackedAppInfo> {
        return if (query.isBlank()) {
            apps
        } else {
            apps.filter { 
                it.appName.contains(query, ignoreCase = true) || 
                it.packageName.contains(query, ignoreCase = true)
            }
        }
    }
}
