package com.example.soundforsilence.data

import com.example.soundforsilence.domain.model.Category
import com.example.soundforsilence.domain.model.Video
import com.example.soundforsilence.domain.repository.VideoRepository
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : VideoRepository {

    // ------------------------------
    // 1️⃣ Categories as a Flow
    // Collection: Video/{categoryId}
    // ------------------------------
    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        val registration = firestore.collection("Video")
            .orderBy("order")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Don’t crash UI – just log and emit empty list
                    println("🔥 Firestore getCategories error: ${error.message}")
                    trySend(emptyList()).isSuccess
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
                            // totalVideos OR totalVideo (fallback)
                            totalVideos = doc.getLong("totalVideos")?.toInt()
                                ?: doc.getLong("totalVideo")?.toInt()
                                ?: 0,
                            completedVideos = 0 // progress will come later
                        )
                    }
                    trySend(list).isSuccess
                }
            }

        awaitClose { registration.remove() }
    }

    // ------------------------------
    // 2️⃣ Videos by category as a Flow
    // Path: Video/{categoryId}/videos/{videoId}
    // ------------------------------
    override fun getVideosByCategory(categoryId: String): Flow<List<Video>> = callbackFlow {
        val query = firestore.collection("Video")
            .document(categoryId)
            .collection("videos")
            .orderBy("order")

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("🔥 Firestore getVideosByCategory error: ${error.message}")
                trySend(emptyList()).isSuccess
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.documents.map { doc ->
                    Video(
                        id = doc.id,
                        categoryId = doc.getString("categoryId") ?: categoryId,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        duration = doc.getString("duration") ?: "", // "9:56"
                        thumbnailUrl = doc.getString("thumbnailUrl") ?: "",
                        videoUrl = doc.getString("videoUrl") ?: "",
                        order = doc.getLong("order")?.toInt() ?: 0,
                        isLocked = doc.getBoolean("isLocked") ?: false,
                        watchProgress = 0,
                        isCompleted = false,
                        questions = emptyList()
                    )
                }
                trySend(list).isSuccess
            }
        }

        awaitClose { registration.remove() }
    }

    // ------------------------------
    // 3️⃣ Single video as a Flow
    // Uses collectionGroup("videos") to search all Video/{cat}/videos
    // ------------------------------
    override fun getVideoById(videoId: String): Flow<Video?> = callbackFlow {
        val query = firestore.collectionGroup("videos")

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("🔥 Firestore getVideoById error: ${error.message}")
                trySend(null).isSuccess
                return@addSnapshotListener
            }

            val doc = snapshot
                ?.documents
                ?.firstOrNull { it.id == videoId }

            val video = doc?.let { d ->
                val categoryRef = d.reference.parent.parent
                val categoryId = categoryRef?.id ?: ""

                Video(
                    id = d.id,
                    categoryId = d.getString("categoryId") ?: categoryId,
                    title = d.getString("title") ?: "",
                    description = d.getString("description") ?: "",
                    duration = d.getString("duration") ?: "",
                    thumbnailUrl = d.getString("thumbnailUrl") ?: "",
                    videoUrl = d.getString("videoUrl") ?: "",
                    order = d.getLong("order")?.toInt() ?: 0,
                    isLocked = d.getBoolean("isLocked") ?: false,
                    watchProgress = 0,
                    isCompleted = false,
                    questions = emptyList()
                )
            }

            trySend(video).isSuccess
        }

        awaitClose { registration.remove() }
    }


    // ------------------------------
    // 4️⃣ Progress methods – stub for now
    // ------------------------------
    override suspend fun updateVideoProgress(
        userId: String,
        videoId: String,
        progress: Int
    ): Result<Unit> {
        // TODO: later store per-user progress in Firestore
        return Result.success(Unit)
    }

    override suspend fun markVideoCompleted(
        userId: String,
        videoId: String
    ): Result<Unit> {
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