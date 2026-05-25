package com.example.conscia.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.remote.DeviceRegistrationRepository
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.ApiResponse
import com.example.conscia.data.remote.dto.AuthResponse
import com.example.conscia.data.remote.dto.GoogleLoginRequest
import com.example.conscia.data.remote.dto.LoginRequest
import com.example.conscia.data.remote.dto.RegisterRequest
import com.example.conscia.data.remote.dto.UserData
import com.example.conscia.data.rule.RuleRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(
        val userEmail: String,
        val isOnboardingCompleted: Boolean
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val dataStore: TrackedAppsDataStore,
    private val deviceRegistrationRepository: DeviceRegistrationRepository,
    private val apiService: ConsciaApiService,
    private val ruleRepository: RuleRepository
) : ViewModel() {
    private companion object {
        const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    val lastUsedEmail = dataStore.lastUsedEmailFlow

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val deviceId = deviceRegistrationRepository.ensureRegisteredDevice()
                val response = apiService.login(LoginRequest(email, pass, deviceId))
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    completeAuth(body.data)
                } else {
                    val errorMsg = parseErrorMessage(response)
                    _authState.value = AuthState.Error(
                        if (response.code() == 401) "Email or password is incorrect." else errorMsg
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login failed", e)
                _authState.value = AuthState.Error(
                    if (e is java.net.SocketTimeoutException) {
                        "Server is taking too long. Please try again in a moment."
                    } else if (e is IllegalStateException && !e.message.isNullOrBlank()) {
                        e.message.orEmpty()
                    } else {
                        "Unable to sign in. Please check your connection and try again."
                    }
                )
            }
        }
    }

    fun register(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val deviceId = deviceRegistrationRepository.ensureRegisteredDevice()
                val response = apiService.register(RegisterRequest(email, pass, name, deviceId))
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    completeAuth(body.data)
                } else {
                    _authState.value = AuthState.Error(parseErrorMessage(response))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Registration failed", e)
                _authState.value = AuthState.Error(
                    if (e is IllegalStateException && !e.message.isNullOrBlank()) {
                        e.message.orEmpty()
                    } else {
                        "Unable to create account. Please try again."
                    }
                )
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val deviceId = deviceRegistrationRepository.ensureRegisteredDevice()
                val response = apiService.googleLogin(GoogleLoginRequest(idToken, deviceId))
                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    completeAuth(body.data)
                } else {
                    val errorMsg = parseErrorMessage(response)
                    _authState.value = AuthState.Error("Google sign in failed: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google sign in failed", e)
                _authState.value = AuthState.Error(
                    if (e is IllegalStateException && !e.message.isNullOrBlank()) {
                        e.message.orEmpty()
                    } else {
                        "Google sign in connection error"
                    }
                )
            }
        }
    }

    private suspend fun completeAuth(authData: AuthResponse?) {
        val data = authData ?: throw IllegalStateException("Auth response data is missing.")
        val accessToken = data.accessToken.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Auth response token is missing.")
        val email = data.user.email.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Auth response user email is missing.")
        val authOnboardingCompleted = data.user.isOnboardingCompleted

        ruleRepository.deleteAllLocalRules()
        dataStore.saveAuthToken(
            accessToken,
            email,
            data.user.displayName,
            data.user.avatarUrl,
            authOnboardingCompleted
        )

        val profile = refreshProfileAfterAuth()
        val serverOnboardingCompleted =
            profile?.isOnboardingCompleted ?: authOnboardingCompleted
        val syncedRuleCount = ruleRepository.syncRulesFromServer()
        val isOnboardingCompleted = serverOnboardingCompleted || syncedRuleCount > 0

        dataStore.setOnboardingCompleted(isOnboardingCompleted)

        if (isOnboardingCompleted && !serverOnboardingCompleted) {
            markOnboardingCompletedRemotely()
        }

        _authState.value = AuthState.Success(email, isOnboardingCompleted)
    }

    private suspend fun refreshProfileAfterAuth(): UserData? {
        try {
            val response = apiService.getUserProfile()
            val profile = response.body()?.data

            if (response.code() == 401) {
                dataStore.clearAuth()
                throw IllegalStateException("Session expired. Please sign in again.")
            }

            if (response.isSuccessful && response.body()?.success == true && profile != null) {
                dataStore.updateUserInfo(
                    profile.displayName.orEmpty(),
                    profile.avatarUrl.orEmpty()
                )
                return profile
            }
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Profile refresh after auth failed", e)
        }

        return null
    }

    private suspend fun markOnboardingCompletedRemotely() {
        try {
            apiService.updateUserProfile(mapOf("isOnboardingCompleted" to true))
        } catch (e: Exception) {
            Log.e(TAG, "Unable to update remote onboarding status", e)
        }
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody.isNullOrBlank()) {
                return "An error occurred (Code: ${response.code()})"
            }

            val apiResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
            apiResponse.message
                ?: apiResponse.error
                ?: "An error occurred (Code: ${response.code()})"
        } catch (e: Exception) {
            "An error occurred (Code: ${response.code()})"
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
