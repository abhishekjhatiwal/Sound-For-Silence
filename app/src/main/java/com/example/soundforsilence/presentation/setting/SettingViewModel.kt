package com.example.soundforsilence.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.soundforsilence.domain.model.User
import com.example.soundforsilence.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // 🔹 Expose current logged-in user (same as Home)
    val currentUser: StateFlow<User?> =
        authRepository.getCurrentUser()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )

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

