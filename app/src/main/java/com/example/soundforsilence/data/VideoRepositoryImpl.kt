package com.example.soundforsilence.data

import com.example.soundforsilence.domain.model.Category
import com.example.soundforsilence.domain.model.Video
import com.example.soundforsilence.domain.repository.VideoRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : VideoRepository {

    // ------------------------------
    // Categories as a Flow
    // ------------------------------
    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        val registration = firestore.collection("categories")
            .orderBy("order")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Close the flow with error
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        Category(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            icon = doc.getString("icon") ?: "",
                            order = doc.getLong("order")?.toInt() ?: 0,
                            totalVideos = doc.getLong("totalVideos")?.toInt() ?: 0,
                            completedVideos = 0 // Will be filled from progress later
                        )
                    }
                    trySend(list).isSuccess
                }
            }

        awaitClose { registration.remove() }
    }

    // ------------------------------
    // Videos by category as a Flow
    // ------------------------------
    override fun getVideosByCategory(categoryId: String): Flow<List<Video>> =
        callbackFlow {
            val query = firestore.collection("videos")
                .whereEqualTo("categoryId", categoryId)
                .orderBy("order")

            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        Video(
                            id = doc.id,
                            categoryId = doc.getString("categoryId") ?: categoryId,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            duration = doc.getString("duration") ?: "",           // e.g. "3:25"
                            thumbnailUrl = doc.getString("thumbnailUrl") ?: "",
                            videoUrl = doc.getString("videoUrl") ?: "",
                            order = doc.getLong("order")?.toInt() ?: 0,
                            isLocked = doc.getBoolean("isLocked") ?: false,
                            watchProgress = 0,       // will be filled from progress repo
                            isCompleted = false,     // will be filled from progress repo
                            questions = emptyList()  // TODO: load from subcollection if needed
                        )
                    }
                    trySend(list).isSuccess
                }
            }

            awaitClose { registration.remove() }
        }

    // ------------------------------
    // Single video as a Flow
    // ------------------------------
    override fun getVideoById(videoId: String): Flow<Video?> =
        getVideosByCategory("") // we’ll ignore category here and just listen to doc
            .map { list -> list.find { it.id == videoId } }
    // 👆 If you want a direct document listener instead:
    // override fun getVideoById(videoId: String): Flow<Video?> = callbackFlow { ... }

    // ------------------------------
    // Progress methods (stub for now)
    // You’re moving progress into a separate VideoProgressRepository,
    // so we keep these as no-ops to satisfy the interface.
    // ------------------------------
    override suspend fun updateVideoProgress(
        userId: String,
        videoId: String,
        progress: Int
    ): Result<Unit> {
        // In the new architecture, this should be handled by VideoProgressRepository.
        // For now, we just return success so your app compiles and runs.
        return Result.success(Unit)
    }

    override suspend fun markVideoCompleted(
        userId: String,
        videoId: String
    ): Result<Unit> {
        // Same as above – delegate to updateVideoProgress or to a separate progress repo later.
        return updateVideoProgress(userId, videoId, 100)
    }
}































/*
class VideoRepositoryImpl : VideoRepository {

    // In-memory demo data
    private val categoriesFlow = MutableStateFlow(
        listOf(
            Category(
                id = "cat_detection",
                name = "Detection Stage",
                description = "Helps your child respond to sounds.",
                icon = "ic_detection",
                order = 1,
                totalVideos = 3,
                completedVideos = 1
            ),
            Category(
                id = "cat_discrimination",
                name = "Discrimination Stage",
                description = "Helps your child differentiate sounds.",
                icon = "ic_discrimination",
                order = 2,
                totalVideos = 2,
                completedVideos = 0
            )
        )
    )

    private val videosFlow = MutableStateFlow(
        listOf(
            Video(
                id = "vid_1",
                categoryId = "cat_detection",
                title = "Responding to Name",
                description = "Teach your child to respond to their name.",
                duration = "3:25",
                thumbnailUrl = "",
                videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                order = 1,
                isLocked = false,
                watchProgress = 60,
                isCompleted = false,
                questions = listOf(/* ... */)
            ),
            // other videos...
        )
    )

    override fun getCategories(): Flow<List<Category>> = categoriesFlow

    override fun getVideosByCategory(categoryId: String): Flow<List<Video>> =
        videosFlow.map { list ->
            list.filter { it.categoryId == categoryId }.sortedBy { it.order }
        }

    override fun getVideoById(videoId: String): Flow<Video?> =
        videosFlow.map { list -> list.find { it.id == videoId } }

    override suspend fun updateVideoProgress(
        userId: String,
        videoId: String,
        progress: Int
    ): Result<Unit> {
        // TODO: Persist per-user progress to backend or local DB
        delay(200)
        val list = videosFlow.value.toMutableList()
        val index = list.indexOfFirst { it.id == videoId }
        if (index != -1) {
            val existing = list[index]
            list[index] = existing.copy(
                watchProgress = progress.coerceIn(0, 100),
                isCompleted = progress >= 100
            )
            videosFlow.value = list
        }
        return Result.success(Unit)
    }

    override suspend fun markVideoCompleted(userId: String, videoId: String): Result<Unit> {
        return updateVideoProgress(userId, videoId, 100)
    }
}


 */