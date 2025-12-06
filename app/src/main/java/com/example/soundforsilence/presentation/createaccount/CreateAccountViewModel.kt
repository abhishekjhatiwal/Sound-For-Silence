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

    var phoneNumber by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var childName by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onNameChange(value: String) { name = value }
    fun onPhoneChange(value: String) { phoneNumber = value }
    fun onPasswordChange(value: String) { password = value }
    fun onChildNameChange(value: String) { childName = value }

    fun onCreateAccountClick(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = registerUseCase(name, phoneNumber, password, childName)

            isLoading = false

            result
                .onSuccess { onSuccess() }
                .onFailure { error ->
                    errorMessage = error.message ?: "Failed to create account"
                }
        }
    }
}