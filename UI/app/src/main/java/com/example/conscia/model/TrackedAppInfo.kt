package com.example.conscia.model

data class TrackedAppInfo(
    val appName: String,
    val packageName: String,
    val isSelected: Boolean = false,
    val isRecommended: Boolean = false
)
