package com.example.soundforsilence.domain.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ProfileData(
    val name: String = "",
    val phone: String = "",
    val email: String = ""
)

class ProfileRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun currentUserId(): String? = auth.currentUser?.uid

    suspend fun getProfile(): Result<ProfileData> {
        val uid = currentUserId() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val name = snapshot.getString("name") ?: ""
            val phone = snapshot.getString("phone") ?: ""
            val email = snapshot.getString("email") ?: auth.currentUser?.email.orEmpty()

            Result.success(ProfileData(name, phone, email))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        name: String,
        phone: String,
        email: String,
        newPassword: String?
    ): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        val uid = user.uid

        return try {
            // 1) Update Firestore fields (name, phone, email)
            val userData = mapOf(
                "name" to name,
                "phone" to phone,
                "email" to email
            )

            firestore.collection("users")
                .document(uid)
                .set(userData)
                .await()

            // 2) Update email in Firebase Auth (if changed)
            if (email.isNotBlank() && email != user.email) {
                user.updateEmail(email).await()
            }

            // 3) Update password in Firebase Auth (if provided)
            if (!newPassword.isNullOrBlank()) {
                user.updatePassword(newPassword).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
