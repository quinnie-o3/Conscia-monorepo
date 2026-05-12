package com.example.conscia.ui.rules

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.AppDatabase
import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.data.rule.RuleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    // Validation flags
    val showErrors: Boolean = false
) {
    val isAppValid: Boolean = selectedPackageName.isNotEmpty()
    val isIntentionValid: Boolean = intention.isNotBlank()
    val isLimitValid: Boolean = ((limitHours.toIntOrNull() ?: 0) * 60 + (limitMinutes.toIntOrNull() ?: 0)) > 0
    val isFormValid: Boolean = isAppValid && isIntentionValid && isLimitValid
}

class CreateEditRuleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RuleRepository
    private var currentRuleId: Long? = null

    private val _uiState = MutableStateFlow(CreateEditRuleUiState())
    val uiState: StateFlow<CreateEditRuleUiState> = _uiState.asStateFlow()

    init {
        val ruleDao = AppDatabase.getDatabase(application).ruleDao()
        repository = RuleRepository(ruleDao)
    }

    fun loadRule(ruleId: Long) {
        currentRuleId = ruleId
        _uiState.update { it.copy(isEditMode = true) }
        viewModelScope.launch {
            repository.getRuleById(ruleId)?.let { rule ->
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
                        errorMessage = "A rule for this app already exists. Edit it instead of creating a duplicate."
                    )
                }
                return@launch
            }

            if (state.isEditMode && existingRule != null && existingRule.id != currentRuleId) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Another rule already uses this app. Choose a different app or edit the existing rule."
                    )
                }
                return@launch
            }

            val rule = RuleEntity(
                id = currentRuleId ?: 0,
                packageName = state.selectedPackageName,
                appName = state.selectedAppName,
                intentionLabel = state.intention,
                dailyLimitMinutes = totalMinutes,
                trackingEnabled = state.trackingEnabled,
                warningEnabled = state.warningEnabled,
                updatedAt = System.currentTimeMillis()
            )

            if (state.isEditMode) {
                repository.updateRule(rule)
            } else {
                repository.insertRule(rule)
            }

            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun deleteRule() {
        val id = currentRuleId ?: return
        viewModelScope.launch {
            val rule = repository.getRuleById(id) ?: return@launch
            repository.deleteRule(rule)
            _uiState.update { it.copy(saveSuccess = true) }
        }
    }
}
