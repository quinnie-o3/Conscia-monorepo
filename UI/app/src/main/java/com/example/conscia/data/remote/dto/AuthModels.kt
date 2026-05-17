package com.example.conscia.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String,
    val tempDeviceId: String? = null
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val tempDeviceId: String? = null
)

data class GoogleLoginRequest(
    val idToken: String,
    val tempDeviceId: String? = null
)

data class ResetPasswordRequest(
    val email: String,
    val newPassword: String
)

data class AuthResponse(
    val accessToken: String,
    val user: UserData
)

data class UserData(
    val id: String,
    val email: String,
    val displayName: String?,
    val avatarUrl: String? = null,
    val isOnboardingCompleted: Boolean = false
)
