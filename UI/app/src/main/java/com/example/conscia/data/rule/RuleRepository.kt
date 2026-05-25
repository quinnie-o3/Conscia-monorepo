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
        addSelectedPackage(rule.packageName)
        syncRuleToRemoteIfAuthenticated(rule)
    }

    suspend fun updateRule(rule: RuleEntity) {
        val previousRule = ruleDao.getRuleById(rule.id)
        ruleDao.updateRule(rule)
        if (previousRule != null && previousRule.packageName != rule.packageName) {
            replaceSelectedPackage(previousRule.packageName, rule.packageName)
        } else {
            addSelectedPackage(rule.packageName)
        }
        syncRuleToRemoteIfAuthenticated(rule)
        if (previousRule != null && previousRule.packageName != rule.packageName) {
            deleteRemoteRuleIfAuthenticated(previousRule.packageName)
        }
    }

    suspend fun deleteRule(rule: RuleEntity) {
        ruleDao.deleteRule(rule)
        removeSelectedPackage(rule.packageName)
        deleteRemoteRuleIfAuthenticated(rule.packageName)
    }

    suspend fun deleteAllLocalRules() {
        ruleDao.deleteAllRules()
        dataStore.saveSelectedPackages(emptySet())
    }

    private suspend fun syncRuleToRemoteIfAuthenticated(rule: RuleEntity) {
        val deviceId = dataStore.deviceIdFlow.firstOrNull() ?: dataStore.generateAndSaveDeviceId()
        if (dataStore.accessTokenFlow.firstOrNull() == null) {
            return
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
        try {
            val response = apiService.upsertTrackingRule(request)
            val body = response.body()
            if (!response.isSuccessful || body?.success != true) {
                throw IllegalStateException(body?.message ?: "Failed to sync rule with backend.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun deleteRemoteRuleIfAuthenticated(packageName: String) {
        val deviceId = dataStore.deviceIdFlow.firstOrNull() ?: dataStore.generateAndSaveDeviceId()
        if (dataStore.accessTokenFlow.firstOrNull() == null) {
            return
        }
        try {
            val response = apiService.deleteTrackingRule(deviceId, packageName)
            val body = response.body()
            if (!response.isSuccessful || body?.success != true) {
                throw IllegalStateException(body?.message ?: "Failed to delete rule from backend.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncRulesFromServer(): Int {
        val deviceId = dataStore.deviceIdFlow.firstOrNull() ?: return 0
        if (dataStore.accessTokenFlow.firstOrNull() == null) return 0

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
                dataStore.saveSelectedPackages(remoteRules.map { it.packageName }.toSet())
                return remoteRules.size
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
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

    private suspend fun addSelectedPackage(packageName: String) {
        dataStore.saveSelectedPackages(
            dataStore.selectedPackagesFlow.firstOrNull().orEmpty() + packageName
        )
    }

    private suspend fun removeSelectedPackage(packageName: String) {
        dataStore.saveSelectedPackages(
            dataStore.selectedPackagesFlow.firstOrNull().orEmpty() - packageName
        )
    }

    private suspend fun replaceSelectedPackage(oldPackageName: String, newPackageName: String) {
        dataStore.saveSelectedPackages(
            (dataStore.selectedPackagesFlow.firstOrNull().orEmpty() - oldPackageName) +
                newPackageName
        )
    }
}
