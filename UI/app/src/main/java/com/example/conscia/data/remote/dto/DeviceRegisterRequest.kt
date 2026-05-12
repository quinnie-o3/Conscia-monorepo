package com.example.conscia.data.remote.dto

data class DeviceRegisterRequest(
    val deviceId: String,
    val anonymousUserId: String? = null,
    val deviceName: String? = null,
    val osVersion: String? = null
)
