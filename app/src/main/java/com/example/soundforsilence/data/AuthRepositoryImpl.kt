package com.example.soundforsilence.data

import com.example.soundforsilence.domain.model.User
import com.example.soundforsilence.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: DatabaseReference
) : AuthRepository {

    private val currentUserFlow = MutableStateFlow<User?>(null)

    override fun getCurrentUser() = currentUserFlow.asStateFlow()

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    // scope for background work (safe for lifecycle-tied singletons)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // If an auth session already exists on startup, load the profile into the flow.
        firebaseAuth.currentUser?.let { firebaseUser ->
            scope.launch { loadUserToFlow(firebaseUser.uid, firebaseUser.email ?: "") }
        }

        // Keep flow in sync with auth state changes (login, logout, token refresh)
        firebaseAuth.addAuthStateListener { auth ->
            val fu = auth.currentUser
            if (fu == null) {
                // signed out
                currentUserFlow.value = null
            } else {
                // signed in: ensure we have the profile loaded
                scope.launch { loadUserToFlow(fu.uid, fu.email ?: "") }
            }
        }
    }

    private suspend fun loadUserToFlow(uid: String, fallbackEmail: String = "") {
        try {
            val snapshot = db.child("users").child(uid).get().await()

            if (snapshot.exists()) {
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                val childName = snapshot.child("childName").getValue(String::class.java) ?: ""
                val childAge = snapshot.child("childAge").getValue(Int::class.java) ?: 0
                val implantDate = snapshot.child("implantDate").getValue(Long::class.java) ?: 0L
                val therapistId = snapshot.child("therapistId").getValue(String::class.java) ?: ""

                val user = User(
                    id = uid,
                    name = name.ifBlank { fallbackEmail.substringBefore("@") },
                    phoneNumber = phoneNumber,
                    childName = childName,
                    childAge = childAge,
                    implantDate = implantDate,
                    therapistId = therapistId
                )
                currentUserFlow.value = user
            } else {
                // Create a lightweight user record if DB missing (keeps behavior same as before)
                val displayName =
                    firebaseAuth.currentUser?.displayName ?: fallbackEmail.substringBefore("@")
                val user = User(
                    id = uid,
                    name = displayName,
                    phoneNumber = "",
                    childName = "",
                    childAge = 0,
                    implantDate = 0L,
                    therapistId = ""
                )
                // attempt to write a minimal profile but don't crash if it fails
                try {
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
                } catch (_: Exception) { /* ignore write failures at startup */
                }

                currentUserFlow.value = user
            }
        } catch (e: Exception) {
            // If reading fails, keep currentUserFlow null (or you could set an error state).
            // Log if you want; avoid throwing from init.
            currentUserFlow.value = null
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Please enter email and password"))
            }

            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email.trim(), password)
                .await()

            val firebaseUser =
                authResult.user ?: return Result.failure(Exception("Login failed, user is null"))
            val uid = firebaseUser.uid

            // load or create user record (ensures currentUserFlow is set)
            loadUserToFlow(uid, firebaseUser.email ?: email)

            // return current value (should be set by loadUserToFlow)
            val user = currentUserFlow.value
            if (user != null) Result.success(user) else Result.failure(Exception("Failed to load user after login"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(trimmedEmail, password)
                .await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Registration failed, user is null"))
            val uid = firebaseUser.uid

            val user = User(
                id = uid,
                name = name,
                phoneNumber = phoneNumber,
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

            db.child("users").child(uid).setValue(map).await()

            currentUserFlow.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(name: String, childName: String): Result<Unit> {
        val uid =
            firebaseAuth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        return try {
            val updates = mapOf("name" to name, "childName" to childName)
            db.child("users").child(uid).updateChildren(updates).await()

            val current = currentUserFlow.value
            if (current != null) {
                currentUserFlow.value = current.copy(name = name, childName = childName)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            // auth state listener will set currentUserFlow = null, but do it eagerly too
            currentUserFlow.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}












/*
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



 */

