package com.example.soundforsilence.domain.repository

import com.example.soundforsilence.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun getCurrentUser(): Flow<User?>
    fun getCurrentUserId(): String?
    fun isUserLoggedIn(): Boolean

    // email + password login
    suspend fun login(email: String, password: String): Result<User>

    // ✅ ADD email parameter here to match your implementation
    suspend fun register(
        name: String,
        phoneNumber: String,
        password: String,
        childName: String,
        email: String
    ): Result<User>

    // 👇 NEW: update name & childName
    suspend fun updateUserProfile(
        name: String,
        childName: String
    ): Result<Unit>

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

