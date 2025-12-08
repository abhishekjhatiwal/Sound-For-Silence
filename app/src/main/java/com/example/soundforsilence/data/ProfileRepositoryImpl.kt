package com.example.soundforsilence.data

import android.net.Uri
import com.example.soundforsilence.domain.model.ProfileState
import com.example.soundforsilence.domain.repository.ProfileRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    private fun currentUser() = auth.currentUser

    override suspend fun getProfile(): Result<ProfileState> {
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

    override suspend fun updateProfile(
        name: String,
        phone: String,
        email: String,
        newPassword: String?,
        imageUrl: String?
    ): Result<Unit> {
        val user = currentUser() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val uid = user.uid

            // Just store imageUrl as-is (or ignore it)
            val data = mutableMapOf<String, Any>(
                "name" to name,
                "phone" to phone,
                "email" to email
            )

            if (!imageUrl.isNullOrBlank()) {
                data["imageUrl"] = imageUrl   // expect a real URL in the future
            }

            firestore.collection("users")
                .document(uid)
                .set(data, SetOptions.merge())
                .await()

            if (email.isNotBlank() && email != user.email) {
                user.updateEmail(email).await()
            }

            if (!newPassword.isNullOrBlank()) {
                user.updatePassword(newPassword).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun reauthenticate(email: String, password: String): Result<Unit> {
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

// Update Profile Code with Firebase Storage

/*
override suspend fun updateProfile(
    name: String,
    phone: String,
    email: String,
    newPassword: String?,
    imageUrl: String?
): Result<Unit> {
    val user = currentUser() ?: return Result.failure(Exception("User not logged in"))

    return try {
        val uid = user.uid

        // -----------------------------
        // 1) Handle profile image upload
        // -----------------------------
        var finalImageUrl: String? = imageUrl

        val data = mutableMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "email" to email
        )

        if (!imageUrl.isNullOrBlank()) {
            data["imageUrl"] = imageUrl   // expect a real URL in the future
        }

        if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("content://")) {
            try {
                val uri = Uri.parse(imageUrl)

                val storageRef = storage
                    .reference
                    .child("profileImages")
                    .child("$uid.jpg")

                // Upload selected image
                storageRef.putFile(uri).await()

                // Get public download URL
                finalImageUrl = storageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                // If upload fails, keep old image (do not break whole update)
                return Result.failure(Exception("Failed to upload profile picture"))
            }
        }

        // -----------------------------------
        // 2) Update Firestore user document
        // -----------------------------------
//            val data = mutableMapOf<String, Any>(
//                "name" to name,
//                "phone" to phone,
//                "email" to email
//            )

        if (!finalImageUrl.isNullOrBlank()) {
            data["imageUrl"] = finalImageUrl
        }

        firestore.collection("users")
            .document(uid)
            .set(data, SetOptions.merge())
            .await()

        // -----------------------------------
        // 3) Update email in Firebase Auth
        // -----------------------------------
        if (email.isNotBlank() && email != user.email) {
            user.updateEmail(email).await()
        }

        // -----------------------------------
        // 4) Update password in Firebase Auth
        // -----------------------------------
        if (!newPassword.isNullOrBlank()) {
            user.updatePassword(newPassword).await()
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}




 */






