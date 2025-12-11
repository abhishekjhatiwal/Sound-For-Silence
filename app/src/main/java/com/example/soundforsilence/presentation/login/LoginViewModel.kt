package com.example.soundforsilence.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.usecase.LoginUseCase
import com.example.soundforsilence.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    var identifier by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // expose login status
    var isAuthenticated by mutableStateOf(false)
        private set

    init {
        // 🔥 Listen to authentication changes immediately
        authRepository.getCurrentUser().onEach { user ->
            isAuthenticated = user != null
        }.launchIn(viewModelScope)
    }

    fun onIdentifierChange(value: String) {
        identifier = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (identifier.isBlank() || password.isBlank()) {
                errorMessage = "Please enter email and password"
                return@launch
            }

            if (!identifier.contains("@")) {
                errorMessage = "Please enter a valid email"
                return@launch
            }

            isLoading = true
            errorMessage = null

            val result = loginUseCase(identifier.trim(), password)

            isLoading = false

            result.onSuccess {
                onSuccess()
            }.onFailure {
                errorMessage = it.message ?: "Login failed"
            }
        }
    }
}

