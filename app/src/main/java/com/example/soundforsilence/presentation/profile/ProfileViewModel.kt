package com.example.soundforsilence.presentation.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.repository.ProfileData
import com.example.soundforsilence.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",      // new password only
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, successMessage = null)
            val result = repository.getProfile()
            uiState = result.fold(
                onSuccess = { profile: ProfileData ->
                    uiState.copy(
                        name = profile.name,
                        phone = profile.phone,
                        email = profile.email,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    uiState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile"
                    )
                }
            )
        }
    }

    fun onNameChange(value: String) {
        uiState = uiState.copy(name = value)
    }

    fun onPhoneChange(value: String) {
        uiState = uiState.copy(phone = value)
    }

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value)
    }

    fun saveProfile() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, successMessage = null)

            val result = repository.updateProfile(
                name = uiState.name,
                phone = uiState.phone,
                email = uiState.email,
                newPassword = uiState.password.ifBlank { null }
            )

            uiState = result.fold(
                onSuccess = {
                    uiState.copy(
                        isLoading = false,
                        password = "", // clear after save
                        successMessage = "Profile updated successfully"
                    )
                },
                onFailure = { e ->
                    uiState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update profile"
                    )
                }
            )
        }
    }
}
