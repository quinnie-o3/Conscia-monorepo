package com.example.conscia.domain.usecase

import com.example.conscia.data.rule.RuleEntity
import com.example.conscia.data.rule.RuleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRulesUseCase @Inject constructor(
    private val repository: RuleRepository
) {
    operator fun invoke(): Flow<List<RuleEntity>> = repository.allRules
}
