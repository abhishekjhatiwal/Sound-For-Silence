package com.example.soundforsilence.domain.usecase

import com.example.soundforsilence.domain.model.User
import com.example.soundforsilence.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(identifier: String, password: String) =
        authRepository.login(identifier, password)
}





/*
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String, password: String): Result<User> {
        if (phoneNumber.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Phone number and password cannot be empty"))
        }
        return authRepository.login(phoneNumber, password)
    }
}

 */