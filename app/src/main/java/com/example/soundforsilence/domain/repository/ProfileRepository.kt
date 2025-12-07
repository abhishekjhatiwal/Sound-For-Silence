package com.example.soundforsilence.domain.repository

import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

data class ProfileData(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val photoUrl: String? = null
)

class ProfileRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference.child("profile_images")

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
            val photoUrl = snapshot.getString("photoUrl")

            Result.success(ProfileData(name, phone, email, photoUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBasicProfile(
        name: String,
        phone: String
    ): Result<Unit> {
        val uid = currentUserId() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val userData = mapOf(
                "name" to name,
                "phone" to phone
            )

            firestore.collection("users")
                .document(uid)
                .update(userData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileWithReauth(
        name: String,
        phone: String,
        email: String,
        newPassword: String?,
        currentPassword: String
    ): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        val uid = user.uid
        val currentEmail = user.email ?: return Result.failure(Exception("No email for user"))

        return try {
            // 1) Re-authenticate
            val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
            user.reauthenticate(credential).await()

            // 2) Update Firestore
            val userData = mapOf(
                "name" to name,
                "phone" to phone,
                "email" to email
            )

            firestore.collection("users")
                .document(uid)
                .update(userData)
                .await()

            // 3) Update email in Auth
            if (email.isNotBlank() && email != currentEmail) {
                user.updateEmail(email).await()
            }

            // 4) Update password
            if (!newPassword.isNullOrBlank()) {
                user.updatePassword(newPassword).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImage(imageUri: Uri): Result<String> {
        val uid = currentUserId() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val imageRef = storage.child("$uid.jpg")
            imageRef.putFile(imageUri).await()
            val url = imageRef.downloadUrl.await().toString()

            // Save to Firestore
            firestore.collection("users")
                .document(uid)
                .update("photoUrl", url)
                .await()

            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}









































/*
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


 */