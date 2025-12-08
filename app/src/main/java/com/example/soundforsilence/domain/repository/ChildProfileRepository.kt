package com.example.soundforsilence.domain.repository

import com.example.soundforsilence.domain.model.ChildProfile
import com.google.firebase.database.DatabaseReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class ChildProfileRepository @Inject constructor(
    private val db: DatabaseReference
) {

    // Load child profile for a specific userId
    suspend fun getChildProfile(userId: String): Result<ChildProfile> {
        return try {
            val snapshot = db.child("users")
                .child(userId)
                .child("childProfile")
                .get()
                .await()

            if (!snapshot.exists()) {
                Result.failure(Exception("No child profile found"))
            } else {
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val age = snapshot.child("age").getValue(String::class.java) ?: ""
                val notes = snapshot.child("notes").getValue(String::class.java) ?: ""

                Result.success(ChildProfile(name, age, notes))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Save child profile for a specific userId
    suspend fun saveChildProfile(userId: String, profile: ChildProfile): Result<Unit> {
        return try {
            val map = mapOf(
                "name" to profile.name,
                "age" to profile.age,
                "notes" to profile.notes
            )

            db.child("users")
                .child(userId)
                .child("childProfile")
                .setValue(map)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


