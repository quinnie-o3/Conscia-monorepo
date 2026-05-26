package com.example.conscia.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.ApiResponse
import com.example.conscia.data.remote.dto.ChangePasswordRequest
import com.example.conscia.data.remote.dto.UserData
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: UserData? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isResetFormVisible: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ConsciaApiService,
    private val dataStore: TrackedAppsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = apiService.getUserProfile()
                val body = response.body()
                val user = body?.data

                if (response.isSuccessful && body?.success == true && user != null) {
                    dataStore.updateUserInfo(
                        user.displayName.orEmpty(),
                        user.avatarUrl.orEmpty()
                    )
                    _uiState.update {
                        it.copy(user = user, isLoading = false, errorMessage = null)
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = parseErrorMessage(response))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unable to load profile"
                    )
                }
            }
        }
    }

    fun toggleResetForm() {
        _uiState.update { it.copy(isResetFormVisible = !it.isResetFormVisible, errorMessage = null, successMessage = null) }
    }

    fun updatePassword(oldPass: String, newPass: String, confirmPass: String) {
        if (newPass != confirmPass) {
            _uiState.update { it.copy(errorMessage = "New passwords do not match") }
            return
        }
        
        if (oldPass.isBlank() || newPass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Passwords cannot be empty") }
            return
        }

        if (newPass.length < 8) {
            _uiState.update { it.copy(errorMessage = "New password must be at least 8 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            try {
                val response = apiService.changePassword(
                    ChangePasswordRequest(
                        currentPassword = oldPass,
                        newPassword = newPass
                    )
                )
                val body = response.body()
                val user = body?.data

                if (response.isSuccessful && body?.success == true) {
                    if (user != null) {
                        dataStore.updateUserInfo(
                            user.displayName.orEmpty(),
                            user.avatarUrl.orEmpty()
                        )
                    }
                    _uiState.update { it.copy(
                        user = user ?: it.user,
                        isSaving = false,
                        successMessage = "Password updated successfully",
                        isResetFormVisible = false
                    ) }
                } else {
                    val errorMsg = parseErrorMessage(response)
                    _uiState.update { it.copy(isSaving = false, errorMessage = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Unable to update password"
                    )
                }
            }
        }
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody.isNullOrBlank()) {
                return response.body()?.let { body ->
                    (body as? ApiResponse<*>)?.message
                } ?: "An error occurred (Code: ${response.code()})"
            }

            val apiResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
            apiResponse.message
                ?: apiResponse.error
                ?: "An error occurred (Code: ${response.code()})"
        } catch (e: Exception) {
            "An error occurred (Code: ${response.code()})"
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
