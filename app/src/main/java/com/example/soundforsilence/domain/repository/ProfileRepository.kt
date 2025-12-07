package com.example.soundforsilence.domain.repository

import com.example.soundforsilence.domain.model.ProfileState
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ProfileRepository {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    private fun currentUser() = auth.currentUser

    suspend fun getProfile(): Result<ProfileState> {
        val user = currentUser() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val snapshot = firestore.collection("users")
                .document(user.uid)
                .get()
                .await()

            val name = snapshot.getString("name") ?: ""
            val phone = snapshot.getString("phone") ?: ""
            val email = snapshot.getString("email") ?: user.email.orEmpty()
            val imageUrl = snapshot.getString("imageUrl")

            Result.success(
                ProfileState(
                    name = name,
                    phone = phone,
                    email = email,
                    imageUrl = imageUrl
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        name: String,
        phone: String,
        email: String,
        newPassword: String?,
        imageUrl: String?
    ): Result<Unit> {
        val user = currentUser() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val uid = user.uid

            // 1) Update Firestore (merge so we don’t lose other data)
            val data = mutableMapOf<String, Any>(
                "name" to name,
                "phone" to phone,
                "email" to email
            )
            if (!imageUrl.isNullOrBlank()) {
                data["imageUrl"] = imageUrl
            }

            firestore.collection("users")
                .document(uid)
                .set(data, SetOptions.merge())
                .await()

            // 2) Update email in Auth if changed
            if (email.isNotBlank() && email != user.email) {
                user.updateEmail(email).await()
            }

            // 3) Update password in Auth if provided
            if (!newPassword.isNullOrBlank()) {
                user.updatePassword(newPassword).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reauthenticate(email: String, password: String): Result<Unit> {
        val user = currentUser() ?: return Result.failure(Exception("User not logged in"))
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


