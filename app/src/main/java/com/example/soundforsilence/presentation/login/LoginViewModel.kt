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
import kotlinx.coroutines.launch
import javax.inject.Inject

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
        // initialize immediately from current id to avoid UI flash
        isAuthenticated = authRepository.getCurrentUserId() != null

        // Listen to authentication changes
        authRepository.getCurrentUser()
            .onEach { user ->
                isAuthenticated = user != null
            }
            .launchIn(viewModelScope)
    }

    fun onIdentifierChange(value: String) {
        identifier = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }

    private fun isPhoneNumber(value: String): Boolean {
        // allow optional + then 7-15 digits (adjust length as you prefer)
        val phoneRegex = Regex("^\\+?[0-9]{7,15}\$")
        return phoneRegex.matches(value.trim())
    }

    private fun isEmail(value: String): Boolean {
        return value.contains("@") && value.contains(".")
    }

    private fun isValidIdentifier(value: String): Boolean {
        val v = value.trim()
        return isEmail(v) || isPhoneNumber(v)
    }

    /**
     * Attempts login. onSuccess will be invoked when loginUseCase returns success.
     * Validation accepts either email or phone (see isValidIdentifier).
     */
    fun onLoginClick(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (identifier.isBlank() || password.isBlank()) {
                errorMessage = "Please enter identifier and password"
                return@launch
            }

            if (!isValidIdentifier(identifier)) {
                errorMessage = "Please enter a valid email or phone number"
                return@launch
            }

            isLoading = true
            errorMessage = null

            val result = loginUseCase(identifier.trim(), password)

            isLoading = false

            result.onSuccess {
                // Successful sign-in; authRepository flow will update isAuthenticated
                onSuccess()
            }.onFailure {
                errorMessage = it.message ?: "Login failed"
            }
        }
    }
}


































/*
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


 */
