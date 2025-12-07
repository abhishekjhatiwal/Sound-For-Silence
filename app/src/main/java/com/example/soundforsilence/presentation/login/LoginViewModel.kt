package com.example.soundforsilence.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // Can be phone number OR email
    var identifier by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onIdentifierChange(value: String) {
        identifier = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }

    fun onLoginClick(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Basic validation
            if (identifier.isBlank() || password.isBlank()) {
                errorMessage = "Please enter phone/email and password"
                return@launch
            }

            isLoading = true
            errorMessage = null

            // identifier can be phone or email
            val result = loginUseCase(identifier.trim(), password)
            isLoading = false

            result
                .onSuccess { onSuccess() }
                .onFailure { errorMessage = it.message ?: "Login failed" }
        }
    }
}
