package com.example.conscia.data.rule

import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.TrackingRuleRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleRepository @Inject constructor(
    private val ruleDao: RuleDao,
    private val apiService: ConsciaApiService,
    private val dataStore: TrackedAppsDataStore
) {
    val allRules: Flow<List<RuleEntity>> = ruleDao.getAllRules()

    suspend fun getRuleById(id: Long): RuleEntity? = ruleDao.getRuleById(id)

    suspend fun getRuleByPackageName(packageName: String): RuleEntity? = 
        ruleDao.getRuleByPackageName(packageName)

    suspend fun insertRule(rule: RuleEntity) {
        ruleDao.insertRule(rule)
        syncRuleToRemote(rule)
    }

    suspend fun updateRule(rule: RuleEntity) {
        ruleDao.updateRule(rule)
        syncRuleToRemote(rule)
    }

    suspend fun deleteRule(rule: RuleEntity) {
        ruleDao.deleteRule(rule)
        val deviceId = dataStore.deviceIdFlow.firstOrNull()
        if (deviceId != null) {
            try {
                apiService.deleteTrackingRule(deviceId, rule.packageName)
            } catch (e: Exception) {
                // Background sync will handle cleanup later if needed
            }
        }
    }

    suspend fun deleteAllLocalRules() {
        ruleDao.deleteAllRules()
    }

    private suspend fun syncRuleToRemote(rule: RuleEntity) {
        val deviceId = dataStore.deviceIdFlow.firstOrNull() ?: return
        val accessToken = dataStore.accessTokenFlow.firstOrNull()
        
        if (accessToken != null) {
            try {
                val request = TrackingRuleRequest(
                    deviceId = deviceId,
                    packageName = rule.packageName,
                    appName = rule.appName,
                    intentionLabel = rule.intentionLabel,
                    dailyLimitMinutes = rule.dailyLimitMinutes,
                    trackingEnabled = rule.trackingEnabled,
                    warningEnabled = rule.warningEnabled
                )
                apiService.upsertTrackingRule(request)
            } catch (e: Exception) {
                // Logic retry handles this
            }
        }
    }

    suspend fun syncRulesFromServer() {
        val deviceId = dataStore.deviceIdFlow.firstOrNull() ?: return
        val accessToken = dataStore.accessTokenFlow.firstOrNull() ?: return

        try {
            val response = apiService.getTrackingRules(deviceId)
            if (response.isSuccessful) {
                val remoteRules = response.body()?.data ?: emptyList()
                remoteRules.forEach { remote ->
                    val localRule = RuleEntity(
                        packageName = remote.packageName,
                        appName = remote.appName,
                        intentionLabel = remote.intentionLabel ?: "",
                        dailyLimitMinutes = remote.dailyLimitMinutes,
                        trackingEnabled = remote.trackingEnabled,
                        warningEnabled = remote.warningEnabled,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    val existing = ruleDao.getRuleByPackageName(remote.packageName)
                    if (existing == null) {
                        ruleDao.insertRule(localRule)
                    } else {
                        ruleDao.updateRule(localRule.copy(id = existing.id))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun upsertRuleByPackage(rule: RuleEntity) {
        val existingRule = getRuleByPackageName(rule.packageName)
        if (existingRule == null) {
            insertRule(rule)
        } else {
            updateRule(
                rule.copy(
                    id = existingRule.id,
                    createdAt = existingRule.createdAt,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
