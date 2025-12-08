package com.example.soundforsilence.domain.repository

import com.example.soundforsilence.domain.model.ChildProfile
import com.google.firebase.database.DatabaseReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Singleton
class ChildProfileRepository @Inject constructor(
    private val db: DatabaseReference
) {

    // Get child profile for a given userId
    suspend fun getChildProfile(userId: String): Result<ChildProfile> =
        suspendCancellableCoroutine { cont ->

            db.child("users")
                .child(userId)
                .child("childProfile")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        // If no profile exists yet, you can either:
                        // 1) treat it as empty profile, or
                        // 2) treat it as error. Here I'm returning an empty profile.
                        val emptyProfile = ChildProfile(
                            name = "",
                            age = "",
                            notes = ""
                        )
                        cont.resume(Result.success(emptyProfile))
                    } else {
                        val name = snapshot.child("name").getValue(String::class.java) ?: ""
                        val age = snapshot.child("age").getValue(String::class.java) ?: ""
                        val notes = snapshot.child("notes").getValue(String::class.java) ?: ""

                        cont.resume(Result.success(ChildProfile(name, age, notes)))
                    }
                }
                .addOnFailureListener { e ->
                    cont.resume(Result.failure(e))
                }
        }

    // Save child profile for a given userId
    suspend fun saveChildProfile(userId: String, profile: ChildProfile): Result<Unit> =
        suspendCancellableCoroutine { cont ->

            val map = mapOf(
                "name" to profile.name,
                "age" to profile.age,
                "notes" to profile.notes
            )

            db.child("users")
                .child(userId)
                .child("childProfile")
                .setValue(map)
                .addOnSuccessListener { cont.resume(Result.success(Unit)) }
                .addOnFailureListener { e -> cont.resume(Result.failure(e)) }
        }
}


































/*
@Singleton
class ChildProfileRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: DatabaseReference
) {

    // Match your signature: Result<ChildProfile>
    suspend fun getChildProfile(): Result<ChildProfile> =
        suspendCancellableCoroutine { cont ->
            val uid = auth.currentUser?.uid
            if (uid == null) {
                cont.resume(Result.failure(IllegalStateException("User not logged in")))
                return@suspendCancellableCoroutine
            }

            db.child("users")
                .child(uid)
                .child("childProfile")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        cont.resume(
                            Result.failure(
                                NoSuchElementException("No child profile found")
                            )
                        )
                    } else {
                        val name = snapshot.child("name").getValue(String::class.java) ?: ""
                        val age = snapshot.child("age").getValue(String::class.java) ?: ""
                        val notes = snapshot.child("notes").getValue(String::class.java) ?: ""

                        cont.resume(Result.success(ChildProfile(name, age, notes)))
                    }
                }
                .addOnFailureListener { e ->
                    cont.resume(Result.failure(e))
                }
        }

    // Match your signature: Result<Unit>
    suspend fun saveChildProfile(profile: ChildProfile): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            val uid = auth.currentUser?.uid
            if (uid == null) {
                cont.resume(Result.failure(IllegalStateException("User not logged in")))
                return@suspendCancellableCoroutine
            }

            val map = mapOf(
                "name" to profile.name,
                "age" to profile.age,
                "notes" to profile.notes
            )

            db.child("users")
                .child(uid)
                .child("childProfile")
                .setValue(map)
                .addOnSuccessListener { cont.resume(Result.success(Unit)) }
                .addOnFailureListener { e -> cont.resume(Result.failure(e)) }
        }
}


 */
