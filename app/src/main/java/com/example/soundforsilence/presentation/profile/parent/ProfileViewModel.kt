package com.example.soundforsilence.presentation.profile.parent

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.model.ProfileState
import com.example.soundforsilence.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    // Keep original email to detect changes & for reauth
    private var originalEmail: String? = null
    private var requireReauthForEmail: Boolean = false
    private var requireReauthForPassword: Boolean = false

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, successMessage = null)
            val result = repository.getProfile()
            _state.value = result.fold(
                onSuccess = { data ->
                    originalEmail = data.email
                    _state.value.copy(
                        name = data.name,
                        phone = data.phone,
                        email = data.email,
                        imageUrl = data.imageUrl,
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
        val emailChanged = value != originalEmail
        requireReauthForEmail = emailChanged
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChanged(value: String) {
        requireReauthForPassword = value.isNotBlank()
        _state.value = _state.value.copy(newPassword = value)
    }

    fun onImageSelected(uri: Uri) {
        _state.value = _state.value.copy(imageUrl = uri.toString())
    }

    fun saveProfile() {
        val s = _state.value

        // If changing email or password, ask for reauth first
        if (requireReauthForEmail || requireReauthForPassword) {
            _state.value = s.copy(
                showReauthDialog = true,
                error = null,          // clear old errors
                successMessage = null
            )
            return
        }

        actuallySaveProfile()
    }

    private fun actuallySaveProfile() {
        val s = _state.value
        viewModelScope.launch {
            _state.value = s.copy(loading = true, error = null, successMessage = null)

            val result = repository.updateProfile(
                name = s.name,
                phone = s.phone,
                email = s.email,
                newPassword = s.newPassword.ifBlank { null },
                imageUrl = s.imageUrl
            )

            _state.value = result.fold(
                onSuccess = {
                    originalEmail = s.email
                    requireReauthForEmail = false
                    requireReauthForPassword = false

                    _state.value.copy(
                        loading = false,
                        successMessage = "Profile updated successfully",
                        newPassword = ""
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

    fun confirmReauth(password: String) {
        val s = _state.value
        val emailForReauth = originalEmail ?: s.email

        if (password.isBlank()) {
            _state.value = s.copy(error = "Please enter your current password")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(loading = true, error = null)

            val result = repository.reauthenticate(emailForReauth, password)

            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        showReauthDialog = false,
                        loading = false
                    )
                    actuallySaveProfile()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Re-authentication failed"
                    )
                }
            )
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }
}

















/*
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    // Keep original email to detect changes & for reauth
    private var originalEmail: String? = null
    private var requireReauthForEmail: Boolean = false
    private var requireReauthForPassword: Boolean = false

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, successMessage = null)
            val result = repository.getProfile()
            _state.value = result.fold(
                onSuccess = { data ->
                    originalEmail = data.email
                    _state.value.copy(
                        name = data.name,
                        phone = data.phone,
                        email = data.email,
                        imageUrl = data.imageUrl,
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
        val emailChanged = value != originalEmail
        requireReauthForEmail = emailChanged
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChanged(value: String) {
        requireReauthForPassword = value.isNotBlank()
        _state.value = _state.value.copy(newPassword = value)
    }

    fun onImageSelected(uri: Uri) {
        // If you later upload to Storage, change this to the download URL
        _state.value = _state.value.copy(imageUrl = uri.toString())
    }

    fun saveProfile() {
        val s = _state.value

        // If changing email or password, ask for reauth first
        if (requireReauthForEmail || requireReauthForPassword) {
            _state.value = s.copy(
                showReauthDialog = true,
                error = null,
                successMessage = null
            )
            return
        }

        actuallySaveProfile()
    }

    private fun actuallySaveProfile() {
        val s = _state.value
        viewModelScope.launch {
            _state.value = s.copy(loading = true, error = null, successMessage = null)

            val result = repository.updateProfile(
                name = s.name,
                phone = s.phone,
                email = s.email,
                newPassword = s.newPassword.ifBlank { null },
                imageUrl = s.imageUrl
            )

            _state.value = result.fold(
                onSuccess = {
                    originalEmail = s.email
                    requireReauthForEmail = false
                    requireReauthForPassword = false

                    _state.value.copy(
                        loading = false,
                        successMessage = "Profile updated successfully",
                        newPassword = ""
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

    fun confirmReauth(password: String) {
        val s = _state.value
        val emailForReauth = originalEmail ?: s.email

        viewModelScope.launch {
            _state.value = s.copy(loading = true, error = null)

            val result = repository.reauthenticate(emailForReauth, password)

            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        showReauthDialog = false,
                        loading = false
                    )
                    actuallySaveProfile()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Re-authentication failed"
                    )
                }
            )
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }
}



 */