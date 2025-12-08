package com.example.soundforsilence.presentation.createaccount

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    var name by mutableStateOf("")
        private set

    // Phone number OR email (for now we treat this as email for Firebase)
    var contact by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var childName by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onNameChange(value: String) {
        name = value
    }

    fun onContactChange(value: String) {
        contact = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }

    fun onChildNameChange(value: String) {
        childName = value
    }

    fun onCreateAccountClick(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val email = contact.trim()

            // Simple validation (optional but recommended)
            if (name.isBlank() || childName.isBlank() || email.isBlank()) {
                errorMessage = "Please fill all fields"
                return@launch
            }
            if (!email.contains("@")) {
                errorMessage = "Please enter a valid email address"
                return@launch
            }
            if (password.length < 6) {
                errorMessage = "Password must be at least 6 characters"
                return@launch
            }

            isLoading = true
            errorMessage = null

            val result = registerUseCase(
                name = name,
                phoneNumber = contact, // currently same as email; later you can add real phone
                password = password,
                childName = childName,
                email = email          // 👈 this fixes "No value passed for parameter 'email'"
            )

            isLoading = false

            result
                .onSuccess { onSuccess() }
                .onFailure { error ->
                    errorMessage = error.message ?: "Failed to create account"
                }
        }
    }
}

















/*
@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    var name by mutableStateOf("")
        private set

    // Phone number OR email
    var contact by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var childName by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onNameChange(value: String) {
        name = value
    }

    fun onContactChange(value: String) {
        contact = value
    }

    fun onPasswordChange(value: String) {
        password = value
    }

    fun onChildNameChange(value: String) {
        childName = value
    }

    fun onCreateAccountClick(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || childName.isBlank() || contact.isBlank()) {
                errorMessage = "Please fill all fields"
                return@launch
            }
            if (!contact.contains("@")) {
                errorMessage = "Please enter a valid email address"
                return@launch
            }
            if (password.length < 6) {
                errorMessage = "Password must be at least 6 characters"
                return@launch
            }

            isLoading = true
            errorMessage = null

            val result = registerUseCase(name, contact, password, childName)

            isLoading = false

            result
                .onSuccess { onSuccess() }
                .onFailure { error ->
                    errorMessage = error.message ?: "Failed to create account"
                }
        }
    }

}

 */