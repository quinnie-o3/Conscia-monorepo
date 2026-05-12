package com.example.conscia.data.rule

import kotlinx.coroutines.flow.Flow

class RuleRepository(private val ruleDao: RuleDao) {
    val allRules: Flow<List<RuleEntity>> = ruleDao.getAllRules()

    suspend fun getRuleById(id: Long): RuleEntity? = ruleDao.getRuleById(id)

    suspend fun getRuleByPackageName(packageName: String): RuleEntity? = 
        ruleDao.getRuleByPackageName(packageName)

    suspend fun insertRule(rule: RuleEntity) = ruleDao.insertRule(rule)

    suspend fun upsertRuleByPackage(rule: RuleEntity) {
        val existingRule = getRuleByPackageName(rule.packageName)
        if (existingRule == null) {
            insertRule(rule)
            return
        }

        updateRule(
            rule.copy(
                id = existingRule.id,
                createdAt = existingRule.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateRule(rule: RuleEntity) = ruleDao.updateRule(rule)

    suspend fun deleteRule(rule: RuleEntity) = ruleDao.deleteRule(rule)
}
