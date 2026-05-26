package com.example.conscia.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.UserData
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
    private val apiService: ConsciaApiService
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
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.update { it.copy(user = response.body()?.data, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load profile") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
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

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            try {
                val response = apiService.updateUserProfile(mapOf(
                    "oldPassword" to oldPass,
                    "password" to newPass
                ))
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.update { it.copy(
                        isSaving = false, 
                        successMessage = "Password updated successfully",
                        isResetFormVisible = false
                    ) }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to update password"
                    _uiState.update { it.copy(isSaving = false, errorMessage = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
