package com.example.soundforsilence.data

import com.example.soundforsilence.domain.model.VideoProgress
import com.example.soundforsilence.domain.repository.VideoProgressRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class VideoProgressRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : VideoProgressRepository {

    override suspend fun saveProgress(
        userId: String,
        progress: VideoProgress
    ): Result<Unit> {
        return try {
            val data = mapOf(
                "videoId" to progress.videoId,
                "categoryId" to progress.categoryId,
                "positionMs" to progress.positionMs,
                "durationMs" to progress.durationMs,
                "completed" to progress.completed,
                "updatedAt" to progress.updatedAt
            )

            firestore.collection("users")
                .document(userId)
                .collection("videoProgress")
                .document(progress.videoId)
                .set(data, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProgressForVideo(
        userId: String,
        videoId: String
    ): Result<VideoProgress?> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("videoProgress")
                .document(videoId)
                .get()
                .await()

            if (!snapshot.exists()) return Result.success(null)

            val p = VideoProgress(
                videoId = snapshot.getString("videoId") ?: videoId,
                categoryId = snapshot.getString("categoryId") ?: "",
                positionMs = snapshot.getLong("positionMs") ?: 0L,
                durationMs = snapshot.getLong("durationMs") ?: 0L,
                completed = snapshot.getBoolean("completed") ?: false,
                updatedAt = snapshot.getLong("updatedAt") ?: 0L
            )

            Result.success(p)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProgressForUser(userId: String): Result<List<VideoProgress>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("videoProgress")
                .get()
                .await()

            val list = snapshot.documents.map { doc ->
                VideoProgress(
                    videoId = doc.getString("videoId") ?: doc.id,
                    categoryId = doc.getString("categoryId") ?: "",
                    positionMs = doc.getLong("positionMs") ?: 0L,
                    durationMs = doc.getLong("durationMs") ?: 0L,
                    completed = doc.getBoolean("completed") ?: false,
                    updatedAt = doc.getLong("updatedAt") ?: 0L
                )
            }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
