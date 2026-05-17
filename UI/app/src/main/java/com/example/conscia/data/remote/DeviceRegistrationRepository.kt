package com.example.conscia.data.remote

import android.os.Build
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.DeviceRegisterRequest
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRegistrationRepository @Inject constructor(
    private val apiService: ConsciaApiService,
    private val dataStore: TrackedAppsDataStore
) {
    suspend fun ensureRegisteredDevice(): String {
        val deviceId = dataStore.deviceIdFlow.firstOrNull()
            ?: dataStore.generateAndSaveDeviceId()

        runCatching {
            apiService.registerDevice(
                DeviceRegisterRequest(
                    deviceId = deviceId,
                    anonymousUserId = deviceId,
                    deviceName = listOf(Build.MANUFACTURER, Build.MODEL)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                        .ifBlank { "Android device" },
                    osVersion = Build.VERSION.RELEASE
                )
            )
        }

        return deviceId
    }
}
