package com.example.conscia.ui.rules

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.AppDatabase
import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.data.rule.RuleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RulesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RuleRepository

    val rulesState: StateFlow<List<RuleEntity>>

    init {
        val ruleDao = AppDatabase.getDatabase(application).ruleDao()
        repository = RuleRepository(ruleDao)
        rulesState = repository.allRules.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
}
