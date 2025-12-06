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

    override fun getCurrentUser(): Flow<User?> = currentUserFlow.asStateFlow()

    override suspend fun isUserLoggedIn(): Boolean = currentUserFlow.value != null
}
