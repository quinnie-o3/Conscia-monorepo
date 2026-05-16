package com.example.conscia.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.IntentionRemote
import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.domain.usecase.DeleteRuleUseCase
import com.example.conscia.domain.usecase.GetRuleByIdUseCase
import com.example.conscia.domain.usecase.UpsertRuleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateEditRuleUiState(
    val selectedPackageName: String = "",
    val selectedAppName: String = "",
    val intention: String = "",
    val limitHours: String = "0",
    val limitMinutes: String = "15",
    val trackingEnabled: Boolean = true,
    val warningEnabled: Boolean = true,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val showErrors: Boolean = false,
    val availableIntentions: List<String> = emptyList(),
    val isLoadingIntentions: Boolean = false
) {
    val isAppValid: Boolean = selectedPackageName.isNotEmpty()
    val isIntentionValid: Boolean = intention.isNotBlank()
    val isLimitValid: Boolean = ((limitHours.toIntOrNull() ?: 0) * 60 + (limitMinutes.toIntOrNull() ?: 0)) > 0
    val isFormValid: Boolean = isAppValid && isIntentionValid && isLimitValid
}

@HiltViewModel
class CreateEditRuleViewModel @Inject constructor(
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
    private val upsertRuleUseCase: UpsertRuleUseCase,
    private val deleteRuleUseCase: DeleteRuleUseCase,
    private val repository: RuleRepository,
    private val apiService: ConsciaApiService
) : ViewModel() {

    private var currentRuleId: Long? = null

    private val _uiState = MutableStateFlow(CreateEditRuleUiState())
    val uiState: StateFlow<CreateEditRuleUiState> = _uiState.asStateFlow()

    init {
        fetchIntentions()
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

    fun createCustomIntention(label: String) {
        viewModelScope.launch {
            try {
                val response = apiService.createIntention(mapOf("label" to label))
                if (response.isSuccessful) {
                    fetchIntentions() // Refresh list
                    onIntentionChanged(label) // Select it
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun loadRule(ruleId: Long) {
        if (ruleId == -1L) return
        currentRuleId = ruleId
        _uiState.update { it.copy(isEditMode = true) }
        viewModelScope.launch {
            getRuleByIdUseCase(ruleId)?.let { rule ->
                _uiState.update { state ->
                    state.copy(
                        selectedPackageName = rule.packageName,
                        selectedAppName = rule.appName,
                        intention = rule.intentionLabel,
                        limitHours = (rule.dailyLimitMinutes / 60).toString(),
                        limitMinutes = (rule.dailyLimitMinutes % 60).toString(),
                        trackingEnabled = rule.trackingEnabled,
                        warningEnabled = rule.warningEnabled
                    )
                }
            }
        }
    }

    fun onAppSelected(packageName: String, appName: String) {
        _uiState.update {
            it.copy(
                selectedPackageName = packageName,
                selectedAppName = appName
            )
        }
    }

    fun onIntentionChanged(value: String) {
        _uiState.update { it.copy(intention = value) }
    }

    fun onLimitHoursChanged(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.update { it.copy(limitHours = value) }
        }
    }

    fun onLimitMinutesChanged(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.update { it.copy(limitMinutes = value) }
        }
    }

    fun onTrackingEnabledChanged(value: Boolean) {
        _uiState.update { it.copy(trackingEnabled = value) }
    }

    fun onWarningEnabledChanged(value: Boolean) {
        _uiState.update { it.copy(warningEnabled = value) }
    }

    fun saveRule() {
        val state = _uiState.value
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val totalMinutes = (state.limitHours.toIntOrNull() ?: 0) * 60 + (state.limitMinutes.toIntOrNull() ?: 0)
            val existingRule = repository.getRuleByPackageName(state.selectedPackageName)
            
            if (!state.isEditMode && existingRule != null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "A rule for this app already exists. Edit it instead."
                    )
                }
                return@launch
            }

            val rule = RuleEntity(
                id = if (state.isEditMode) currentRuleId ?: 0 else 0,
                packageName = state.selectedPackageName,
                appName = state.selectedAppName,
                intentionLabel = state.intention,
                dailyLimitMinutes = totalMinutes,
                trackingEnabled = state.trackingEnabled,
                warningEnabled = state.warningEnabled,
                updatedAt = System.currentTimeMillis()
            )

            upsertRuleUseCase(rule)

            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun deleteRule() {
        val id = currentRuleId ?: return
        viewModelScope.launch {
            val rule = getRuleByIdUseCase(id) ?: return@launch
            deleteRuleUseCase(rule)
            _uiState.update { it.copy(saveSuccess = true) }
        }
    }
}
