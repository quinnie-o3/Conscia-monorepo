package com.example.conscia.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.AppRepository
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.data.rule.RuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StarterRuleDraft(
    val packageName: String,
    val appName: String,
    val intentionLabel: String = ""
)

data class StarterRulesUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val drafts: List<StarterRuleDraft> = emptyList(),
    val errorMessage: String? = null,
    val availableIntentions: List<String> = emptyList(),
    val isLoadingIntentions: Boolean = false
) {
    val hasSelectedApps: Boolean = drafts.isNotEmpty()
    val canContinue: Boolean = drafts.isNotEmpty() && drafts.all { it.intentionLabel.isNotBlank() } && !isSaving
}

@HiltViewModel
class StarterRulesViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val dataStore: TrackedAppsDataStore,
    private val ruleRepository: RuleRepository,
    private val apiService: ConsciaApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StarterRulesUiState())
    val uiState: StateFlow<StarterRulesUiState> = _uiState.asStateFlow()

    init {
        loadDrafts()
        fetchIntentions()
    }

    private fun loadDrafts() {
        viewModelScope.launch {
            val selectedPackages = dataStore.selectedPackagesFlow.first()
            val installedApps = appRepository.getInstalledApps()
            val drafts = installedApps
                .filter { it.packageName in selectedPackages }
                .map { StarterRuleDraft(packageName = it.packageName, appName = it.appName) }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    drafts = drafts,
                    errorMessage = null
                )
            }
        }
    }

    fun fetchIntentions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingIntentions = true) }
            try {
                val response = apiService.getIntentions()
                if (response.isSuccessful) {
                    val intentions = response.body()?.data?.map { it.label } ?: emptyList()
                    _uiState.update { it.copy(availableIntentions = intentions, isLoadingIntentions = false) }
                } else {
                    _uiState.update { it.copy(isLoadingIntentions = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingIntentions = false) }
            }
        }
    }

    fun createCustomIntention(label: String, forPackage: String) {
        viewModelScope.launch {
            try {
                val response = apiService.createIntention(mapOf("label" to label))
                if (response.isSuccessful) {
                    fetchIntentions()
                    onIntentionSelected(forPackage, label)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun onIntentionSelected(packageName: String, intentionLabel: String) {
        _uiState.update { state ->
            state.copy(
                drafts = state.drafts.map { draft ->
                    if (draft.packageName == packageName) {
                        draft.copy(intentionLabel = intentionLabel)
                    } else {
                        draft
                    }
                },
                errorMessage = null
            )
        }
    }

    fun saveStarterRules(onComplete: () -> Unit) {
        val drafts = _uiState.value.drafts
        if (drafts.isEmpty()) {
            onComplete()
            return
        }

        if (drafts.any { it.intentionLabel.isBlank() }) {
            _uiState.update { it.copy(errorMessage = "Choose a reason for every selected app.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            drafts.forEach { draft ->
                ruleRepository.upsertRuleByPackage(
                    RuleEntity(
                        packageName = draft.packageName,
                        appName = draft.appName,
                        intentionLabel = draft.intentionLabel,
                        dailyLimitMinutes = 60, // Tăng lên 60 phút để tránh báo "vượt hạn mức" ngay lập tức
                        trackingEnabled = true,
                        warningEnabled = true
                    )
                )
            }

            _uiState.update { it.copy(isSaving = false) }
            onComplete()
        }
    }
}
