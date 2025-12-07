package com.example.soundforsilence.presentation.profile.parent

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.repository.ProfileData
import com.example.soundforsilence.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val newPassword: String = "",
    val imageUrl: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showReauthDialog: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    private var originalEmail: String? = null

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, successMessage = null)

            val result = repository.getProfile()
            _state.value = result.fold(
                onSuccess = { data: ProfileData ->
                    originalEmail = data.email
                    _state.value.copy(
                        name = data.name,
                        phone = data.phone,
                        email = data.email,
                        imageUrl = data.photoUrl,
                        loading = false
                    )
                },
                onFailure = { e ->
                    _state.value.copy(
                        loading = false,
                        error = e.message ?: "Failed to load profile"
                    )
                }
            )
        }
    }

    fun onNameChanged(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun onPhoneChanged(value: String) {
        _state.value = _state.value.copy(phone = value)
    }

    fun onEmailChanged(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChanged(value: String) {
        _state.value = _state.value.copy(newPassword = value)
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, successMessage = null)
            val result = repository.uploadProfileImage(uri)
            _state.value = result.fold(
                onSuccess = { url ->
                    _state.value.copy(
                        imageUrl = url,
                        loading = false,
                        successMessage = "Profile picture updated"
                    )
                },
                onFailure = { e ->
                    _state.value.copy(
                        loading = false,
                        error = e.message ?: "Failed to upload image"
                    )
                }
            )
        }
    }

    fun saveProfile() {
        val current = _state.value
        val emailChanged = current.email != originalEmail
        val passwordProvided = current.newPassword.isNotBlank()

        if (emailChanged || passwordProvided) {
            // Need re-authentication
            _state.value = current.copy(
                showReauthDialog = true,
                error = null,
                successMessage = null
            )
        } else {
            // Only basic data changed, no reauth
            updateBasicProfile()
        }
    }

    private fun updateBasicProfile() {
        viewModelScope.launch {
            val s = _state.value
            _state.value = s.copy(loading = true, error = null, successMessage = null)
            val result = repository.updateBasicProfile(
                name = s.name,
                phone = s.phone
            )
            _state.value = result.fold(
                onSuccess = {
                    _state.value.copy(
                        loading = false,
                        successMessage = "Profile updated"
                    )
                },
                onFailure = { e ->
                    _state.value.copy(
                        loading = false,
                        error = e.message ?: "Failed to update profile"
                    )
                }
            )
        }
    }

    fun dismissReauthDialog() {
        _state.value = _state.value.copy(showReauthDialog = false)
    }

    fun confirmReauth(currentPassword: String) {
        viewModelScope.launch {
            val s = _state.value
            _state.value = s.copy(loading = true, error = null, successMessage = null, showReauthDialog = false)

            val result = repository.updateProfileWithReauth(
                name = s.name,
                phone = s.phone,
                email = s.email,
                newPassword = s.newPassword.ifBlank { null },
                currentPassword = currentPassword
            )

            _state.value = result.fold(
                onSuccess = {
                    originalEmail = s.email
                    _state.value.copy(
                        loading = false,
                        newPassword = "",
                        successMessage = "Profile updated"
                    )
                },
                onFailure = { e ->
                    _state.value.copy(
                        loading = false,
                        error = e.message ?: "Re-authentication failed"
                    )
                }
            )
        }
    }
}


























/*
data class ProfileState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val newPassword: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)

            val result = repository.getProfile()
            result.onSuccess { data ->
                _state.value = _state.value.copy(
                    name = data.name,
                    phone = data.phone,
                    email = data.email,
                    loading = false
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    loading = false,
                    error = it.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun onNameChanged(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun onPhoneChanged(value: String) {
        _state.value = _state.value.copy(phone = value)
    }

    fun onEmailChanged(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChanged(value: String) {
        _state.value = _state.value.copy(newPassword = value)
    }

    fun saveProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, successMessage = null)

            val result = repository.updateProfile(
                name = _state.value.name,
                phone = _state.value.phone,
                email = _state.value.email,
                newPassword = _state.value.newPassword.ifBlank { null }
            )

            result.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    successMessage = "Profile updated successfully!"
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    loading = false,
                    error = it.message ?: "Error updating profile"
                )
            }
        }
    }
}


 */