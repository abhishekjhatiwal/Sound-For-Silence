package com.example.soundforsilence.presentation.setting

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var isLoggingOut by mutableStateOf(false)
        private set

    var logoutError by mutableStateOf<String?>(null)
        private set

    fun onLogoutClick(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoggingOut = true
            logoutError = null

            val result = authRepository.logout()

            isLoggingOut = false

            result
                .onSuccess { onSuccess() }
                .onFailure { throwable ->
                    logoutError = throwable.message ?: "Logout failed"
                }
        }
    }
}
