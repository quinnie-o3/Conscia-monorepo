package com.example.conscia.domain.usecase

import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.data.rule.RuleRepository
import javax.inject.Inject

class GetRuleByIdUseCase @Inject constructor(
    private val repository: RuleRepository
) {
    suspend operator fun invoke(id: Long): RuleEntity? {
        return repository.getRuleById(id)
    }
}
