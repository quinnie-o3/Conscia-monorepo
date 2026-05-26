package com.example.conscia.data.rule

import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.ApiResponse
import com.example.conscia.data.remote.dto.TrackingRuleRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleRepository @Inject constructor(
    private val ruleDao: RuleDao,
    private val apiService: ConsciaApiService,
    private val dataStore: TrackedAppsDataStore
) {
    val allRules: Flow<List<RuleEntity>> = ruleDao.getAllRules()
    private val gson = Gson()
    private val remoteDateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    )

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
        val response = apiService.upsertTrackingRule(request)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            throw IllegalStateException(
                parseErrorMessage(response, "Failed to sync rule with backend.")
            )
        }
    }

    private suspend fun deleteRemoteRuleIfAuthenticated(packageName: String) {
        val deviceId = dataStore.deviceIdFlow.firstOrNull() ?: dataStore.generateAndSaveDeviceId()
        if (dataStore.accessTokenFlow.firstOrNull() == null) {
            return
        }
        val response = apiService.deleteTrackingRule(deviceId, packageName)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            throw IllegalStateException(
                parseErrorMessage(response, "Failed to delete rule from backend.")
            )
        }
    }

    suspend fun syncRulesFromServer(): Int {
        val deviceId = dataStore.deviceIdFlow.firstOrNull() ?: return 0
        if (dataStore.accessTokenFlow.firstOrNull() == null) return 0

        try {
            val response = apiService.getTrackingRules(deviceId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                val remoteRules = body.data ?: emptyList()
                val localRules = allRules.first()
                val localRulesByPackage = localRules.associateBy { it.packageName }
                val remotePackages = remoteRules.map { it.packageName }.toSet()
                val now = System.currentTimeMillis()

                remoteRules.forEach { remote ->
                    val existingLocalRule = localRulesByPackage[remote.packageName]
                    val remoteUpdatedAt = parseRemoteTimestamp(remote.updatedAt)
                    val localIsNewer = existingLocalRule != null &&
                        remoteUpdatedAt != null &&
                        existingLocalRule.updatedAt > remoteUpdatedAt

                    if (localIsNewer) {
                        runCatching { syncRuleToRemoteIfAuthenticated(existingLocalRule) }
                        return@forEach
                    }

                    ruleDao.insertRule(
                        RuleEntity(
                            id = existingLocalRule?.id ?: 0,
                            packageName = remote.packageName,
                            appName = remote.appName,
                            intentionLabel = remote.intentionLabel ?: "",
                            dailyLimitMinutes = remote.dailyLimitMinutes,
                            trackingEnabled = remote.trackingEnabled,
                            warningEnabled = remote.warningEnabled,
                            extensionMinutes = remote.extensionMinutes ?: 0,
                            extensionCount = remote.extensionCount ?: 0,
                            lastExtensionDate = remote.lastExtensionDate.orEmpty(),
                            createdAt = existingLocalRule?.createdAt
                                ?: parseRemoteTimestamp(remote.createdAt)
                                ?: now,
                            updatedAt = remoteUpdatedAt ?: existingLocalRule?.updatedAt ?: now
                        )
                    )
                }

                val localOnlyRules = localRules.filter { it.packageName !in remotePackages }
                localOnlyRules.forEach { rule ->
                    runCatching { syncRuleToRemoteIfAuthenticated(rule) }
                }

                dataStore.saveSelectedPackages(
                    (remotePackages + localOnlyRules.map { it.packageName }).toSet()
                )
                return remoteRules.size + localOnlyRules.size
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

    private fun parseRemoteTimestamp(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        return remoteDateFormats.firstNotNullOfOrNull { format ->
            runCatching {
                synchronized(format) {
                    format.parse(value)?.time
                }
            }.getOrNull()
        }
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>, fallback: String): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody.isNullOrBlank()) {
                return (response.body() as? ApiResponse<*>)?.message
                    ?: fallback
            }

            val apiResponse = gson.fromJson(errorBody, ApiResponse::class.java)
            apiResponse.message ?: apiResponse.error ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }
}
