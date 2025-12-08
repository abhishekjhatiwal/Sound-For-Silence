package com.example.soundforsilence.domain.repository

import com.example.soundforsilence.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    // Expose the current user as a Flow
    fun getCurrentUser(): Flow<User?>

    // Convenience helpers we are using in ViewModels
    fun getCurrentUserId(): String?
    fun isUserLoggedIn(): Boolean

    // Auth operations
    suspend fun login(identifier: String, password: String): Result<User>

    suspend fun register(
        name: String,
        phoneNumber: String,
        password: String,
        childName: String
    ): Result<User>

    suspend fun logout(): Result<Unit>
}




















/*
interface AuthRepository {
    // identifier = phone number OR email
    suspend fun login(identifier: String, password: String): Result<User>

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


 */

