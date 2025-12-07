package com.example.soundforsilence.domain.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ChildProfileData(
    val name: String = "",
    val age: String = "",
    val notes: String = ""
)

class ChildProfileRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun currentUserId(): String? = auth.currentUser?.uid

    suspend fun getChildProfile(): Result<ChildProfileData> {
        val uid = currentUserId() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val name = snapshot.getString("childName") ?: ""
            val age = snapshot.getString("childAge") ?: ""
            val notes = snapshot.getString("childNotes") ?: ""

            Result.success(ChildProfileData(name, age, notes))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveChildProfile(
        name: String,
        age: String,
        notes: String
    ): Result<Unit> {
        val uid = currentUserId() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val childData = mapOf(
                "childName" to name,
                "childAge" to age,
                "childNotes" to notes
            )

            firestore.collection("users")
                .document(uid)
                .update(childData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
