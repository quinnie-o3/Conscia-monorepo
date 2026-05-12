package com.example.conscia.data.remote.dto

data class ApiResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)
