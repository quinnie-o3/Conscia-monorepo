package com.example.conscia.domain.usecase

import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.data.rule.RuleRepository
import javax.inject.Inject

class UpsertRuleUseCase @Inject constructor(
    private val repository: RuleRepository
) {
    suspend operator fun invoke(rule: RuleEntity) {
        if (rule.id == 0L) {
            repository.insertRule(rule)
        } else {
            repository.updateRule(rule)
        }
    }
}
