package com.example.conscia.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.domain.usecase.GetRulesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val getRulesUseCase: GetRulesUseCase
) : ViewModel() {

    val rulesState: StateFlow<List<RuleEntity>> = getRulesUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
