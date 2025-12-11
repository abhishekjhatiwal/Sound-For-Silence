package com.example.soundforsilence.data

import com.example.soundforsilence.domain.model.Category
import com.example.soundforsilence.domain.model.Video
import com.example.soundforsilence.domain.repository.VideoRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : VideoRepository {

    private val CLOUDINARY_CLOUD_NAME: String = try {
        val name = com.example.soundforsilence.BuildConfig.CLOUDINARY_CLOUD_NAME
        if (name.isNotBlank()) name else ""
    } catch (_: Throwable) {
        ""
    }

    private fun extractPlayableUrl(doc: com.google.firebase.firestore.DocumentSnapshot): String? {
        return listOf(
            "fileUrl", "videoUrl", "secure_url", "secureUrl",
            "url", "video_url", "file_url", "public_url"
        ).firstNotNullOfOrNull { key ->
            doc.getString(key)?.takeIf { it.isNotBlank() }
        }
    }

    private fun computeThumbnailUrlIfMissing(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        currentThumb: String?
    ): String {
        if (!currentThumb.isNullOrBlank()) return currentThumb
        val storagePath = doc.getString("storagePath") ?: doc.getString("storage_path")
        ?: doc.getString("publicId") ?: ""
        if (storagePath.isBlank()) return ""
        if (CLOUDINARY_CLOUD_NAME.isBlank()) return ""
        return "https://res.cloudinary.com/$CLOUDINARY_CLOUD_NAME/video/upload/w_400,h_225,c_fill,q_auto,f_auto/$storagePath.jpg"
    }

    // ------------------------------
    // 1️⃣ Categories as a Flow (single-count aggregation)
    // ------------------------------
    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        val registration = firestore.collection("categories")
            .orderBy("order")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("🔥 getCategories listener error: ${error.message}")
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }

                // Map categories (initial)
                val cats = snapshot.documents.map { doc ->
                    Category(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        icon = doc.getString("icon") ?: "",
                        order = doc.getLong("order")?.toInt() ?: 0,
                        totalVideos = 0,
                        completedVideos = 0
                    )
                }

                // Emit initial (counts 0) so UI isn't blocked
                trySend(cats).isSuccess

                // Aggregate counts with one query over videos
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val videosSnap = firestore.collection("videos").get().await()
                        // build a map categoryId -> count
                        val counts = mutableMapOf<String, Int>()
                        videosSnap.documents.forEach { d ->
                            val catId = d.getString("categoryId") ?: ""
                            if (catId.isNotBlank()) counts[catId] = (counts[catId] ?: 0) + 1
                        }
                        val catsWithCounts = cats.map { c ->
                            c.copy(totalVideos = counts[c.id] ?: 0)
                        }
                        trySend(catsWithCounts).isSuccess
                    } catch (e: Exception) {
                        println("🔥 getCategories: failed aggregating video counts: ${e.message}")
                        // still send original cats if aggregation fails
                        trySend(cats).isSuccess
                    }
                }
            }

        awaitClose { registration.remove() }
    }

    // ------------------------------
    // 2️⃣ Videos by category as a Flow (robust debug + fallback)
    // ------------------------------
    override fun getVideosByCategory(categoryId: String): Flow<List<Video>> = callbackFlow {
        // Primary query: where + orderBy (may require composite index)
        val primaryQuery = firestore.collection("videos")
            .whereEqualTo("categoryId", categoryId)
            .orderBy("position")

        val primaryReg = primaryQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("🔥 Firestore getVideosByCategory primary error: ${error.message}")
                // Fallback: try simple whereEqualTo WITHOUT orderBy
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val fallbackSnap = firestore.collection("videos")
                            .whereEqualTo("categoryId", categoryId)
                            .get()
                            .await()
                        println("🔁 Fallback getVideosByCategory returned ${fallbackSnap.size()} docs for category=$categoryId")
                        val list = fallbackSnap.documents.map { doc ->
                            val playable = extractPlayableUrl(doc) ?: ""
                            val thumb =
                                computeThumbnailUrlIfMissing(doc, doc.getString("thumbnailUrl"))
                            Video(
                                id = doc.id,
                                categoryId = doc.getString("categoryId") ?: categoryId,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                duration = doc.getString("duration") ?: "",
                                thumbnailUrl = thumb,
                                videoUrl = playable,
                                order = (doc.getLong("position") ?: doc.getLong("order")
                                ?: 0L).toInt(),
                                isLocked = doc.getBoolean("isLocked") ?: false,
                                watchProgress = 0,
                                isCompleted = false,
                                questions = emptyList()
                            )
                        }
                        trySend(list).isSuccess
                    } catch (e2: Exception) {
                        println("🔥 Fallback getVideosByCategory failed: ${e2.message}")
                        trySend(emptyList()).isSuccess
                    }
                }
                return@addSnapshotListener
            }

            if (snapshot != null) {
                println("✅ getVideosByCategory snapshot (${snapshot.size()}) for category=$categoryId")
                snapshot.documents.forEach { d ->
                    println(
                        "  -> video doc ${d.id}: keys=${d.data?.keys} fileUrl=${d.getString("fileUrl")} categoryId=${
                            d.getString(
                                "categoryId"
                            )
                        }"
                    )
                }
                val list = snapshot.documents.map { doc ->
                    val playable = extractPlayableUrl(doc) ?: ""
                    val thumb = computeThumbnailUrlIfMissing(doc, doc.getString("thumbnailUrl"))
                    Video(
                        id = doc.id,
                        categoryId = doc.getString("categoryId") ?: categoryId,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        duration = doc.getString("duration") ?: "",
                        thumbnailUrl = thumb,
                        videoUrl = playable,
                        order = (doc.getLong("position") ?: doc.getLong("order") ?: 0L).toInt(),
                        isLocked = doc.getBoolean("isLocked") ?: false,
                        watchProgress = 0,
                        isCompleted = false,
                        questions = emptyList()
                    )
                }
                trySend(list).isSuccess
            }
        }

        awaitClose { primaryReg.remove() }
    }

    // ------------------------------
    // 3️⃣ Single video as a Flow
    // ------------------------------
    override fun getVideoById(videoId: String): Flow<Video?> = callbackFlow {
        val ref = firestore.collection("videos").document(videoId)
        val reg = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("🔥 getVideoById error: ${error.message}")
                trySend(null).isSuccess
                return@addSnapshotListener
            }
            val doc = snapshot ?: run {
                trySend(null).isSuccess
                return@addSnapshotListener
            }

            val playable = extractPlayableUrl(doc) ?: ""
            val thumb = computeThumbnailUrlIfMissing(doc, doc.getString("thumbnailUrl"))
            val video = Video(
                id = doc.id,
                categoryId = doc.getString("categoryId") ?: "",
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                duration = doc.getString("duration") ?: "",
                thumbnailUrl = thumb,
                videoUrl = playable,
                order = (doc.getLong("position") ?: doc.getLong("order") ?: 0L).toInt(),
                isLocked = doc.getBoolean("isLocked") ?: false,
                watchProgress = 0,
                isCompleted = false,
                questions = emptyList()
            )
            trySend(video).isSuccess
        }
        awaitClose { reg.remove() }
    }

    // ------------------------------
    // 4️⃣ Progress methods (unchanged)
    // ------------------------------
    override suspend fun updateVideoProgress(
        userId: String,
        videoId: String,
        progress: Int
    ): Result<Unit> {
        return try {
            val data = mapOf(
                "progressPercent" to progress,
                "positionMs" to 0L,
                "durationMs" to 0L,
                "completed" to (progress >= 100),
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .collection("videoProgress")
                .document(videoId)
                .set(data, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markVideoCompleted(userId: String, videoId: String): Result<Unit> {
        return try {
            val data = mapOf(
                "progressPercent" to 100,
                "positionMs" to 0L,
                "durationMs" to 0L,
                "completed" to true,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .collection("videoProgress")
                .document(videoId)
                .set(data, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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