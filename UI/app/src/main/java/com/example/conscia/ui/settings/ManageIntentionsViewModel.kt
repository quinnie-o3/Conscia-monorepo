package com.example.conscia.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conscia.data.remote.api.ConsciaApiService
import com.example.conscia.data.remote.dto.IntentionRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageIntentionsUiState(
    val intentions: List<IntentionRemote> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ManageIntentionsViewModel @Inject constructor(
    private val apiService: ConsciaApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageIntentionsUiState())
    val uiState: StateFlow<ManageIntentionsUiState> = _uiState.asStateFlow()

    init {
        fetchIntentions()
    }

    fun fetchIntentions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = apiService.getIntentions()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(
                        intentions = response.body()?.data ?: emptyList(),
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load: ${response.message()}"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error"
                ) }
            }
        }
    }

    fun deleteIntention(id: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteIntention(id)
                if (response.isSuccessful) {
                    fetchIntentions() // Refresh list
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
