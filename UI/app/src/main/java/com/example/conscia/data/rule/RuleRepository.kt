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
        syncRuleToRemote(rule)
        ruleDao.insertRule(rule)
    }

    suspend fun updateRule(rule: RuleEntity) {
        val previousRule = ruleDao.getRuleById(rule.id)
        syncRuleToRemote(rule)
        if (previousRule != null && previousRule.packageName != rule.packageName) {
            deleteRemoteRule(previousRule.packageName)
        }
        ruleDao.updateRule(rule)
    }

    suspend fun deleteRule(rule: RuleEntity) {
        deleteRemoteRule(rule.packageName)
        ruleDao.deleteRule(rule)
    }

    suspend fun deleteAllLocalRules() {
        ruleDao.deleteAllRules()
    }

    private suspend fun syncRuleToRemote(rule: RuleEntity) {
        val deviceId = dataStore.deviceIdFlow.firstOrNull() ?: dataStore.generateAndSaveDeviceId()
        if (dataStore.accessTokenFlow.firstOrNull() == null) {
            throw IllegalStateException("Please sign in before saving rules.")
        }

        val request = TrackingRuleRequest(
            deviceId = deviceId,
            packageName = rule.packageName,
            appName = rule.appName,
            intentionLabel = rule.intentionLabel,
            dailyLimitMinutes = rule.dailyLimitMinutes,
            trackingEnabled = rule.trackingEnabled,
            warningEnabled = rule.warningEnabled,
            extensionMinutes = rule.extensionMinutes,
            extensionCount = rule.extensionCount,
            lastExtensionDate = rule.lastExtensionDate.ifBlank { null }
        )
        val response = apiService.upsertTrackingRule(request)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            throw IllegalStateException(body?.message ?: "Failed to sync rule with backend.")
        }
    }

    private suspend fun deleteRemoteRule(packageName: String) {
        val deviceId = dataStore.deviceIdFlow.firstOrNull() ?: dataStore.generateAndSaveDeviceId()
        if (dataStore.accessTokenFlow.firstOrNull() == null) {
            throw IllegalStateException("Please sign in before deleting rules.")
        }
        val response = apiService.deleteTrackingRule(deviceId, packageName)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            throw IllegalStateException(body?.message ?: "Failed to delete rule from backend.")
        }
    }

    suspend fun syncRulesFromServer() {
        val deviceId = dataStore.deviceIdFlow.firstOrNull() ?: return
        val accessToken = dataStore.accessTokenFlow.firstOrNull() ?: return

        try {
            val response = apiService.getTrackingRules(deviceId)
            if (response.isSuccessful) {
                val remoteRules = response.body()?.data ?: emptyList()
                ruleDao.deleteAllRules()
                remoteRules.forEach { remote ->
                    val localRule = RuleEntity(
                        packageName = remote.packageName,
                        appName = remote.appName,
                        intentionLabel = remote.intentionLabel ?: "",
                        dailyLimitMinutes = remote.dailyLimitMinutes,
                        trackingEnabled = remote.trackingEnabled,
                        warningEnabled = remote.warningEnabled,
                        extensionMinutes = remote.extensionMinutes ?: 0,
                        extensionCount = remote.extensionCount ?: 0,
                        lastExtensionDate = remote.lastExtensionDate.orEmpty(),
                        updatedAt = System.currentTimeMillis()
                    )
                    ruleDao.insertRule(localRule)
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
