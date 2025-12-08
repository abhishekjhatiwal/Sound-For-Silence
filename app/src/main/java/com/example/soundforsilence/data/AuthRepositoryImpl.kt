package com.example.soundforsilence.data

import com.example.soundforsilence.domain.model.User
import com.example.soundforsilence.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: DatabaseReference // only if you actually use it
) : AuthRepository {

    private val currentUserFlow = MutableStateFlow<User?>(null)

    // 🔹 required by interface
    override fun getCurrentUser(): Flow<User?> = currentUserFlow.asStateFlow()

    // 🔹 convenience: used in ViewModels
    override fun getCurrentUserId(): String? {
        // VERY IMPORTANT: always read from FirebaseAuth
        return firebaseAuth.currentUser?.uid
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun login(identifier: String, password: String): Result<User> {
        // TODO: Replace this with real Firebase sign-in when ready
        delay(500) // simulate network delay

        val trimmed = identifier.trim()
        val isEmail = trimmed.contains("@")

        // Basic validation
        if (trimmed.isBlank()) {
            return Result.failure(Exception("Please enter phone number or email ID"))
        }
        if (password.length < 4) {
            return Result.failure(Exception("Password must be at least 4 characters"))
        }

        if (isEmail) {
            if (!trimmed.contains(".") || trimmed.length < 5) {
                return Result.failure(Exception("Please enter a valid email address"))
            }
        } else {
            if (trimmed.length < 10) {
                return Result.failure(Exception("Please enter a valid mobile number"))
            }
        }

        // Demo user – replace with real Firebase user info later
        val user = User(
            id = "user_001",
            name = "Parent User",
            phoneNumber = trimmed,
            childName = "Child Name",
            childAge = 4,
            implantDate = System.currentTimeMillis(),
            therapistId = "therapist_123"
        )

        // 🔹 update Flow
        currentUserFlow.value = user

        return Result.success(user)
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

        // 🔹 update Flow
        currentUserFlow.value = user

        return Result.success(user)
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            currentUserFlow.value = null  // 🔹 clear in-memory user too
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
