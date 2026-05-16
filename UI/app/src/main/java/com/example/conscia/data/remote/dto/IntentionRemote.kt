package com.example.conscia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class IntentionRemote(
    @SerializedName("_id") val id: String,
    val label: String,
    val isSystem: Boolean,
    val userId: String?
)
