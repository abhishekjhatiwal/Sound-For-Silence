package com.example.soundforsilence.domain.repository

import com.example.soundforsilence.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(phoneNumber: String, password: String): Result<User>
    suspend fun register(
        name: String,
        phoneNumber: String,
        password: String,
        childName: String
    ): Result<User>

    suspend fun logout(): Result<Unit>
    fun getCurrentUser(): Flow<User?>
    suspend fun isUserLoggedIn(): Boolean
}