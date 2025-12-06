package com.example.soundforsilence.data

import com.example.soundforsilence.domain.model.User
import com.example.soundforsilence.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl : AuthRepository {

    private val currentUserFlow = MutableStateFlow<User?>(null)

    override suspend fun login(phoneNumber: String, password: String): Result<User> {
        // TODO: Replace with real backend / Firebase auth
        delay(500) // simulate network delay

        return if (phoneNumber.length >= 10 && password.length >= 4) {
            val user = User(
                id = "user_001",
                name = "Parent User",
                phoneNumber = phoneNumber,
                childName = "Child Name",
                childAge = 4,
                implantDate = System.currentTimeMillis(),
                therapistId = "therapist_123"
            )
            currentUserFlow.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid phone or password"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        currentUserFlow.value = null
        return Result.success(Unit)
    }

    override suspend fun register(
        name: String,
        phoneNumber: String,
        password: String,
        childName: String
    ): Result<User> {
        delay(500) // simulate network / API call

        if (name.isBlank() || childName.isBlank()) {
            return Result.failure(Exception("Name and child name cannot be empty"))
        }
        if (phoneNumber.length < 10) {
            return Result.failure(Exception("Please enter a valid mobile number"))
        }
        if (password.length < 4) {
            return Result.failure(Exception("Password must be at least 4 characters"))
        }

        val user = User(
            id = "user_002",
            name = name,
            phoneNumber = phoneNumber,
            childName = childName,
            childAge = 4,
            implantDate = System.currentTimeMillis(),
            therapistId = "therapist_123"
        )
        currentUserFlow.value = user
        return Result.success(user)
    }

    override fun getCurrentUser(): Flow<User?> = currentUserFlow.asStateFlow()

    override suspend fun isUserLoggedIn(): Boolean = currentUserFlow.value != null
}
