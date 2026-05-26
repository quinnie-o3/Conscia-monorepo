package com.example.conscia.ui.tracked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.domain.model.TrackedAppLimitInfo
import com.example.conscia.domain.usecase.EvaluateTrackedAppsUsageUseCase
import com.example.conscia.domain.usecase.GetRulesUseCase
import com.example.conscia.domain.usecase.GetTodayUsageUseCase
import com.example.conscia.domain.usecase.DeleteRuleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackedAppsUiState(
    val isLoading: Boolean = true,
    val apps: List<TrackedAppLimitInfo> = emptyList(),
    val errorMessage: String? = null
)

data class TrackedAppDetailUiState(
    val isLoading: Boolean = true,
    val rule: RuleEntity? = null,
    val todayUsageMillis: Long = 0L,
    val todayLaunchCount: Int = 0,
    val isEditingLimit: Boolean = false,
    val isEditingReason: Boolean = false,
    val limitHours: String = "0",
    val limitMinutes: String = "1",
    val reason: String = "",
    val errorMessage: String? = null
) {
    val savedLimitMinutes: Int = rule?.dailyLimitMinutes ?: 0
    val savedReason: String = rule?.intentionLabel.orEmpty()
    val limitMillis: Long = savedLimitMinutes.toLong() * 60_000L
    val usagePercent: Float = if (limitMillis > 0L) {
        todayUsageMillis.toFloat() / limitMillis.toFloat()
    } else {
        0f
    }
}

@HiltViewModel
class TrackedAppsViewModel @Inject constructor(
    private val getRulesUseCase: GetRulesUseCase,
    private val getTodayUsageUseCase: GetTodayUsageUseCase,
    private val evaluateUseCase: EvaluateTrackedAppsUsageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackedAppsUiState())
    val uiState: StateFlow<TrackedAppsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val rules = getRulesUseCase().first()
                val usage = getTodayUsageUseCase()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        apps = evaluateUseCase.execute(rules, usage),
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load rules: ${e.message}")
                }
            }
        }
    }
}

@HiltViewModel
class TrackedAppDetailViewModel @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val deleteRuleUseCase: DeleteRuleUseCase,
    private val getTodayUsageUseCase: GetTodayUsageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackedAppDetailUiState())
    val uiState: StateFlow<TrackedAppDetailUiState> = _uiState.asStateFlow()

    private var currentRuleId: Long = -1L

    fun load(ruleId: Long) {
        if (currentRuleId == ruleId && _uiState.value.rule != null) return
        currentRuleId = ruleId
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val rule = ruleRepository.getRuleById(currentRuleId)
                val todayUsage = getTodayUsageUseCase()
                val appUsageMillis = todayUsage
                    .firstOrNull { it.packageName == rule?.packageName }
                    ?.totalTimeInForegroundMillis ?: 0L
                val appLaunchCount = todayUsage
                    .firstOrNull { it.packageName == rule?.packageName }
                    ?.launchCount ?: 0

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rule = rule,
                        todayUsageMillis = appUsageMillis,
                        todayLaunchCount = appLaunchCount,
                        limitHours = ((rule?.dailyLimitMinutes ?: 1) / 60).toString(),
                        limitMinutes = ((rule?.dailyLimitMinutes ?: 1) % 60).toString(),
                        reason = rule?.intentionLabel.orEmpty(),
                        isEditingLimit = false,
                        isEditingReason = false,
                        errorMessage = if (rule == null) "Rule not found." else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load app info: ${e.message}")
                }
            }
        }
    }

    fun startLimitEdit() {
        val rule = _uiState.value.rule ?: return
        _uiState.update {
            it.copy(
                isEditingLimit = true,
                limitHours = (rule.dailyLimitMinutes / 60).toString(),
                limitMinutes = (rule.dailyLimitMinutes % 60).toString(),
                errorMessage = null
            )
        }
    }

    fun startReasonEdit() {
        val rule = _uiState.value.rule ?: return
        _uiState.update {
            it.copy(
                isEditingReason = true,
                reason = rule.intentionLabel,
                errorMessage = null
            )
        }
    }

    fun cancelLimitEdit() {
        val rule = _uiState.value.rule ?: return
        _uiState.update {
            it.copy(
                isEditingLimit = false,
                limitHours = (rule.dailyLimitMinutes / 60).toString(),
                limitMinutes = (rule.dailyLimitMinutes % 60).toString(),
                errorMessage = null
            )
        }
    }

    fun cancelReasonEdit() {
        val rule = _uiState.value.rule ?: return
        _uiState.update {
            it.copy(isEditingReason = false, reason = rule.intentionLabel, errorMessage = null)
        }
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

    fun onReasonChanged(value: String) {
        _uiState.update { it.copy(reason = value) }
    }

    fun saveLimit() {
        val state = _uiState.value
        val rule = state.rule ?: return
        val totalMinutes = (state.limitHours.toIntOrNull() ?: 0) * 60 + (state.limitMinutes.toIntOrNull() ?: 0)

        if (totalMinutes <= 0) {
            _uiState.update { it.copy(errorMessage = "Daily limit must be greater than 0 minutes.") }
            return
        }

        viewModelScope.launch {
            try {
                val updatedRule = rule.copy(
                    dailyLimitMinutes = totalMinutes,
                    updatedAt = System.currentTimeMillis()
                )
                ruleRepository.updateRule(updatedRule)
                _uiState.update {
                    it.copy(
                        rule = updatedRule,
                        isEditingLimit = false,
                        limitHours = (totalMinutes / 60).toString(),
                        limitMinutes = (totalMinutes % 60).toString(),
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to save limit.")
                }
            }
        }
    }

    fun saveReason() {
        val state = _uiState.value
        val rule = state.rule ?: return
        val reason = state.reason.trim()

        if (reason.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Reason cannot be empty.") }
            return
        }

        viewModelScope.launch {
            try {
                val updatedRule = rule.copy(
                    intentionLabel = reason,
                    updatedAt = System.currentTimeMillis()
                )
                ruleRepository.updateRule(updatedRule)
                _uiState.update {
                    it.copy(
                        rule = updatedRule,
                        isEditingReason = false,
                        reason = reason,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to save reason.")
                }
            }
        }
    }

    fun deleteRule(onDeleted: () -> Unit) {
        val rule = _uiState.value.rule ?: return
        viewModelScope.launch {
            try {
                deleteRuleUseCase(rule)
                onDeleted()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Failed to delete rule.")
                }
            }
        }
    }
}
