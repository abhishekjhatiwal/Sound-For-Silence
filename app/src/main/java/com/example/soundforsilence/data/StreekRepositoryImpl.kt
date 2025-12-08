package com.example.soundforsilence.data

import com.example.soundforsilence.domain.model.StreakInfo
import com.example.soundforsilence.domain.repository.StreakRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

@Singleton
class StreakRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : StreakRepository {

    override suspend fun getStreak(userId: String): Result<StreakInfo?> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("meta")
                .document("streak")
                .get()
                .await()

            if (!doc.exists()) return Result.success(null)

            val info = StreakInfo(
                currentStreak = doc.getLong("currentStreak")?.toInt() ?: 0,
                longestStreak = doc.getLong("longestStreak")?.toInt() ?: 0,
                lastActiveDate = doc.getString("lastActiveDate") ?: ""
            )
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStreakForToday(userId: String, today: String): Result<StreakInfo> {
        return try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("meta")
                .document("streak")

            val snapshot = docRef.get().await()

            val previous = if (snapshot.exists()) {
                StreakInfo(
                    currentStreak = snapshot.getLong("currentStreak")?.toInt() ?: 0,
                    longestStreak = snapshot.getLong("longestStreak")?.toInt() ?: 0,
                    lastActiveDate = snapshot.getString("lastActiveDate") ?: ""
                )
            } else {
                StreakInfo(0, 0, "")
            }

            // today is expected as "YYYY-MM-DD"
            val todayDate = LocalDate.parse(today)
            val yesterday = todayDate.minusDays(1).toString() // also "YYYY-MM-DD"

            val newCurrent = when (previous.lastActiveDate) {
                today -> previous.currentStreak          // already counted today
                yesterday -> previous.currentStreak + 1  // continued streak
                else -> 1                                // reset streak
            }

            val newLongest = maxOf(previous.longestStreak, newCurrent)

            val updated = StreakInfo(
                currentStreak = newCurrent,
                longestStreak = newLongest,
                lastActiveDate = today
            )

            docRef.set(
                mapOf(
                    "currentStreak" to updated.currentStreak,
                    "longestStreak" to updated.longestStreak,
                    "lastActiveDate" to updated.lastActiveDate
                ),
                SetOptions.merge()
            ).await()

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

