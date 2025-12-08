package com.example.soundforsilence.data

import com.example.soundforsilence.domain.model.User
import com.example.soundforsilence.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: DatabaseReference
) : AuthRepository {

    private val currentUserFlow = MutableStateFlow<User?>(null)

    override fun getCurrentUser(): Flow<User?> = currentUserFlow.asStateFlow()

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    // REAL Firebase email+password login
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Please enter email and password"))
            }

            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email.trim(), password)
                .await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Login failed, user is null"))

            val uid = firebaseUser.uid

            val snapshot = db.child("users")
                .child(uid)
                .get()
                .await()

            val user = if (snapshot.exists()) {
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                val childName = snapshot.child("childName").getValue(String::class.java) ?: ""
                val childAge = snapshot.child("childAge").getValue(Int::class.java) ?: 0
                val implantDate = snapshot.child("implantDate").getValue(Long::class.java) ?: 0L
                val therapistId = snapshot.child("therapistId").getValue(String::class.java) ?: ""

                User(
                    id = uid,
                    name = name,
                    phoneNumber = phoneNumber,
                    childName = childName,
                    childAge = childAge,
                    implantDate = implantDate,
                    therapistId = therapistId
                )
            } else {
                val user = User(
                    id = uid,
                    name = firebaseUser.displayName ?: email.substringBefore("@"),
                    phoneNumber = "",
                    childName = "",
                    childAge = 0,
                    implantDate = 0L,
                    therapistId = ""
                )

                db.child("users").child(uid).setValue(
                    mapOf(
                        "name" to user.name,
                        "phoneNumber" to user.phoneNumber,
                        "childName" to user.childName,
                        "childAge" to user.childAge,
                        "implantDate" to user.implantDate,
                        "therapistId" to user.therapistId
                    )
                ).await()

                user
            }

            currentUserFlow.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // REAL Firebase email+password register – now matches interface (5 params)
    override suspend fun register(
        name: String,
        phoneNumber: String,
        password: String,
        childName: String,
        email: String
    ): Result<User> {
        return try {
            val trimmedEmail = email.trim()

            if (name.isBlank() || childName.isBlank() || trimmedEmail.isBlank()) {
                return Result.failure(Exception("Name, child name, and email cannot be empty"))
            }
            if (!trimmedEmail.contains("@")) {
                return Result.failure(Exception("Please enter a valid email address"))
            }
            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }

            // Create Firebase user
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(trimmedEmail, password)
                .await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Registration failed, user is null"))

            val uid = firebaseUser.uid

            // Build domain user
            val user = User(
                id = uid,
                name = name,
                phoneNumber = phoneNumber, // here you can store a real phone if you add that later
                childName = childName,
                childAge = 4,
                implantDate = System.currentTimeMillis(),
                therapistId = "therapist_123"
            )

            val map = mapOf(
                "name" to user.name,
                "phoneNumber" to user.phoneNumber,
                "childName" to user.childName,
                "childAge" to user.childAge,
                "implantDate" to user.implantDate,
                "therapistId" to user.therapistId
            )

            db.child("users")
                .child(uid)
                .setValue(map)
                .await()

            currentUserFlow.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(
        name: String,
        childName: String
    ): Result<Unit> {
        val uid = firebaseAuth.currentUser?.uid
            ?: return Result.failure(Exception("User not logged in"))

        return try {
            // 1) Update in Realtime Database
            val updates = mapOf(
                "name" to name,
                "childName" to childName
            )

            db.child("users")
                .child(uid)
                .updateChildren(updates)
                .await()

            // 2) Update in in-memory currentUserFlow so UI updates immediately
            val current = currentUserFlow.value
            if (current != null) {
                currentUserFlow.value = current.copy(
                    name = name,
                    childName = childName
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            currentUserFlow.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}



