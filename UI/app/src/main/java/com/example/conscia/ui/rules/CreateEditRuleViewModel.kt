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
    val limitHours: String = "00",
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
    val limitHourValue: Int = limitHours.toIntOrNull() ?: 0
    val limitMinuteValue: Int = limitMinutes.toIntOrNull() ?: 0
    val isLimitValid: Boolean = limitHourValue in 0..23 &&
        limitMinuteValue in allowedMinuteValues &&
        (limitHourValue * 60 + limitMinuteValue) >= 15
    val isFormValid: Boolean = isAppValid && isIntentionValid && isLimitValid

    companion object {
        val allowedMinuteValues = setOf(0, 15, 30, 45)
    }
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
                        limitHours = (rule.dailyLimitMinutes / 60).coerceIn(0, 23).toTwoDigitString(),
                        limitMinutes = normalizeMinute(rule.dailyLimitMinutes % 60).toTwoDigitString(),
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
        val hour = value.toIntOrNull() ?: return
        setLimitHour(hour)
    }

    fun onLimitMinutesChanged(value: String) {
        val minute = value.toIntOrNull() ?: return
        setLimitMinute(minute)
    }

    fun setLimitHour(hour: Int) {
        val normalizedHour = hour.coerceIn(0, 23)
        _uiState.update { state ->
            var minute = state.limitMinuteValue
            if (normalizedHour == 0 && minute == 0) {
                minute = 15
            }
            state.copy(
                limitHours = normalizedHour.toTwoDigitString(),
                limitMinutes = normalizeMinute(minute).toTwoDigitString()
            )
        }
    }

    fun setLimitMinute(minute: Int) {
        val normalizedMinute = normalizeMinute(minute)
        _uiState.update { state ->
            val safeMinute = if (state.limitHourValue == 0 && normalizedMinute == 0) 15 else normalizedMinute
            state.copy(limitMinutes = safeMinute.toTwoDigitString())
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
            if (totalMinutes < 15 || state.limitHourValue !in 0..23 || state.limitMinuteValue !in CreateEditRuleUiState.allowedMinuteValues) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showErrors = true,
                        errorMessage = "Limit must be between 00:15 and 23:45, in 15-minute steps."
                    )
                }
                return@launch
            }

            val existingRule = repository.getRuleByPackageName(state.selectedPackageName)
            
            if (existingRule != null && (!state.isEditMode || existingRule.id != currentRuleId)) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "A rule for this app already exists. Edit it instead."
                    )
                }
                return@launch
            }

            val currentRule = currentRuleId?.let { repository.getRuleById(it) }
            val rule = RuleEntity(
                id = if (state.isEditMode) currentRuleId ?: 0 else 0,
                packageName = state.selectedPackageName,
                appName = state.selectedAppName,
                intentionLabel = state.intention,
                dailyLimitMinutes = totalMinutes,
                trackingEnabled = state.trackingEnabled,
                warningEnabled = state.warningEnabled,
                extensionMinutes = currentRule?.extensionMinutes ?: 0,
                extensionCount = currentRule?.extensionCount ?: 0,
                lastExtensionDate = currentRule?.lastExtensionDate ?: "",
                createdAt = currentRule?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            try {
                upsertRuleUseCase(rule)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Failed to save rule."
                    )
                }
            }
        }
    }

    fun deleteRule() {
        val id = currentRuleId ?: return
        viewModelScope.launch {
            val rule = getRuleByIdUseCase(id) ?: return@launch
            try {
                deleteRuleUseCase(rule)
                _uiState.update { it.copy(saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to delete rule.")
                }
            }
        }
    }

    private fun Int.toTwoDigitString(): String = toString().padStart(2, '0')

    private fun normalizeMinute(value: Int): Int {
        return when (value) {
            0, 15, 30, 45 -> value
            else -> (value / 15 * 15).coerceIn(0, 45)
        }
    }
}
