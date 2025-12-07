package com.example.soundforsilence.domain.repository

import com.example.soundforsilence.domain.model.ChildProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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




//class ChildProfileRepository {
//
//    // ✅ same instance as everywhere else
//    private val auth = Firebase.auth
//    private val firestore = FirebaseFirestore.getInstance()
//
//    private fun currentUserId(): String? = auth.currentUser?.uid
//
//    suspend fun getChildProfile(): Result<ChildProfile> {
//        val uid = currentUserId() ?: return Result.failure(Exception("User not logged in"))
//
//        return try {
//            val snapshot = firestore.collection("users")
//                .document(uid)
//                .get()
//                .await()
//
//            val name = snapshot.getString("childName") ?: ""
//            val age = snapshot.getString("childAge") ?: ""
//            val notes = snapshot.getString("childNotes") ?: ""
//
//            Result.success(ChildProfile(name, age, notes))
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun saveChildProfile(
//        name: String,
//        age: String,
//        notes: String
//    ): Result<Unit> {
//        val uid = currentUserId() ?: return Result.failure(Exception("User not logged in"))
//
//        return try {
//            val childData = mapOf(
//                "childName" to name,
//                "childAge" to age,
//                "childNotes" to notes
//            )
//
//            firestore.collection("users")
//                .document(uid)
//                .set(childData, SetOptions.merge())  // ✅ create or update
//                .await()
//
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//}

/*

class FirebaseChildProfileRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: DatabaseReference
) : ChildProfileRepository {

    override suspend fun getChildProfile(): ChildProfile? =
        suspendCancellableCoroutine { cont ->
            val uid = auth.currentUser?.uid ?: run {
                cont.resume(null, null)
                return@suspendCancellableCoroutine
            }

            db.child("users")
                .child(uid)
                .child("childProfile")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java) ?: ""
                        val age = snapshot.child("age").getValue(String::class.java) ?: ""
                        val notes = snapshot.child("notes").getValue(String::class.java) ?: ""

                        cont.resume(
                            ChildProfile(name = name, age = age, notes = notes),
                            null
                        )
                    } else {
                        cont.resume(null, null)
                    }
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }

    override suspend fun saveChildProfile(profile: ChildProfile) =
        suspendCancellableCoroutine { cont ->
            val uid = auth.currentUser?.uid ?: run {
                cont.resume(Unit, null)
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
                .addOnSuccessListener { cont.resume(Unit, null) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
}


 */