package com.example.soundforsilence.domain.repository

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
        childName: String,
        email: String
    ): Result<User> {
        return authRepository.register(
            name = name,
            phoneNumber = phoneNumber,
            password = password,
            childName = childName,
            email = email
        )
    }
}
