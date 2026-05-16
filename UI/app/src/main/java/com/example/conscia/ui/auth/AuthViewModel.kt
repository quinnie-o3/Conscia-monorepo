package com.example.conscia.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.LoginRequest
import com.example.conscia.data.remote.dto.RegisterRequest
import com.example.conscia.data.remote.dto.GoogleLoginRequest
import com.example.conscia.data.remote.dto.ResetPasswordRequest
import com.example.conscia.data.rule.RuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userEmail: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class PasswordResetState {
    object Idle : PasswordResetState()
    object Loading : PasswordResetState()
    data class Success(val message: String) : PasswordResetState()
    data class Error(val message: String) : PasswordResetState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val dataStore: TrackedAppsDataStore,
    private val apiService: ConsciaApiService,
    private val ruleRepository: RuleRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _passwordResetState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val passwordResetState: StateFlow<PasswordResetState> = _passwordResetState
    
    val lastUsedEmail = dataStore.lastUsedEmailFlow

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val deviceId = dataStore.deviceIdFlow.firstOrNull()
                val response = apiService.login(LoginRequest(email, pass, deviceId))
                if (response.isSuccessful && response.body()?.success == true) {
                    val authData = response.body()?.data!!
                    dataStore.saveAuthToken(authData.accessToken, authData.user.email)
                    
                    // Sync rules immediately after login to populate local DB
                    ruleRepository.syncRulesFromServer()
                    
                    _authState.value = AuthState.Success(authData.user.email)
                } else {
                    val errorMsg = response.body()?.message ?: response.message()
                    _authState.value = AuthState.Error(
                        if (response.code() == 401) "Incorrect email or password. Please try again."
                        else "Sign in failed: $errorMsg"
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun register(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val deviceId = dataStore.deviceIdFlow.firstOrNull()
                val response = apiService.register(RegisterRequest(email, pass, name, deviceId))
                if (response.isSuccessful && response.body()?.success == true) {
                    val authData = response.body()?.data!!
                    dataStore.saveAuthToken(authData.accessToken, authData.user.email)
                    
                    // New users won't have rules yet, but sync just in case of data migration
                    ruleRepository.syncRulesFromServer()

                    _authState.value = AuthState.Success(authData.user.email)
                } else {
                    val errorMsg = response.body()?.message ?: response.message()
                    _authState.value = AuthState.Error("Sign up failed: $errorMsg")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val deviceId = dataStore.deviceIdFlow.firstOrNull()
                val response = apiService.googleLogin(GoogleLoginRequest(idToken, deviceId))
                if (response.isSuccessful && response.body()?.success == true) {
                    val authData = response.body()?.data!!
                    dataStore.saveAuthToken(authData.accessToken, authData.user.email)
                    ruleRepository.syncRulesFromServer()
                    _authState.value = AuthState.Success(authData.user.email)
                } else {
                    val errorMsg = response.body()?.message ?: response.message()
                    _authState.value = AuthState.Error("Google sign in failed: $errorMsg")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google sign in failed")
            }
        }
    }

    fun resetPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            _passwordResetState.value = PasswordResetState.Loading
            try {
                val response = apiService.resetPassword(ResetPasswordRequest(email, newPassword))
                if (response.isSuccessful && response.body()?.success == true) {
                    _passwordResetState.value = PasswordResetState.Success("Password updated. Sign in again.")
                } else {
                    val errorMsg = response.body()?.message ?: response.message()
                    _passwordResetState.value = PasswordResetState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _passwordResetState.value = PasswordResetState.Error(e.message ?: "Failed to reset password")
            }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun resetPasswordResetState() {
        _passwordResetState.value = PasswordResetState.Idle
    }
}
