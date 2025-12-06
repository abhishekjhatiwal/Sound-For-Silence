package com.example.soundforsilence.domain.usecase

import com.example.soundforsilence.domain.model.User
import com.example.soundforsilence.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        phoneNumber: String,
        password: String,
        childName: String
    ): Result<User> {
        if (name.isBlank() || childName.isBlank()) {
            return Result.failure(Exception("Name and child name cannot be empty"))
        }
        return authRepository.register(name, phoneNumber, password, childName)
    }
}
