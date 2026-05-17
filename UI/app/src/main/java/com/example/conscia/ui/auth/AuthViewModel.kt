package com.example.conscia.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.remote.DeviceRegistrationRepository
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.LoginRequest
import com.example.conscia.data.remote.dto.RegisterRequest
import com.example.conscia.data.remote.dto.GoogleLoginRequest
import com.example.conscia.data.remote.dto.ApiResponse
import com.example.conscia.data.remote.dto.ResetPasswordRequest
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
    data class Success(val userEmail: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val dataStore: TrackedAppsDataStore,
    private val deviceRegistrationRepository: DeviceRegistrationRepository,
    private val apiService: ConsciaApiService,
    private val ruleRepository: RuleRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    val lastUsedEmail = dataStore.lastUsedEmailFlow

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val deviceId = deviceRegistrationRepository.ensureRegisteredDevice()
                val response = apiService.login(LoginRequest(email, pass, deviceId))
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val authData = response.body()?.data!!
                    // Save full user info to fix "Guest" and missing avatar issues
                    dataStore.saveAuthToken(
                        authData.accessToken,
                        authData.user.email,
                        authData.user.displayName,
                        ""
                    )
                    
                    _authState.value = AuthState.Success(authData.user.email)
                    
                    viewModelScope.launch {
                        try {
                            ruleRepository.syncRulesFromServer()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    val errorMsg = parseErrorMessage(response)
                    _authState.value = AuthState.Error(
                        if (response.code() == 401) "Incorrect email or password. Please try again."
                        else errorMsg
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    if (e is java.net.SocketTimeoutException) "Server is taking too long. Please try again in a moment."
                    else "Connection error. Please check your internet."
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
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val authData = response.body()?.data!!
                    // Save full user info
                    dataStore.saveAuthToken(
                        authData.accessToken,
                        authData.user.email,
                        authData.user.displayName,
                        ""
                    )
                    
                    _authState.value = AuthState.Success(authData.user.email)
                    
                    viewModelScope.launch {
                        try {
                            ruleRepository.syncRulesFromServer()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    val errorMsg = parseErrorMessage(response)
                    _authState.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Connection error. Please try again.")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val deviceId = deviceRegistrationRepository.ensureRegisteredDevice()
                val response = apiService.googleLogin(GoogleLoginRequest(idToken, deviceId))
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val authData = response.body()?.data!!
                    // Save full user info (Fixes Google User appearing as Guest)
                    dataStore.saveAuthToken(
                        authData.accessToken,
                        authData.user.email,
                        authData.user.displayName,
                        ""
                    )
                    
                    _authState.value = AuthState.Success(authData.user.email)
                    
                    viewModelScope.launch {
                        try {
                            ruleRepository.syncRulesFromServer()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    val errorMsg = parseErrorMessage(response)
                    _authState.value = AuthState.Error("Google sign in failed: $errorMsg")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Google sign in connection error")
            }
        }
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val apiResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
            apiResponse.message ?: "An unknown error occurred"
        } catch (e: Exception) {
            "An error occurred (Code: ${response.code()})"
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
