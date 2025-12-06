package com.example.soundforsilence.data

import com.example.soundforsilence.domain.model.Category
import com.example.soundforsilence.domain.model.Video
import com.example.soundforsilence.domain.repository.VideoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

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


    /*
    private val videosFlow = MutableStateFlow(
        listOf(
            Video(
                id = "vid_1",
                categoryId = "cat_detection",
                title = "Responding to Name",
                description = "Teach your child to respond to their name.",
                duration = "3:25",
                thumbnailUrl = "",
                videoUrl = "",
                order = 1,
                isLocked = false,
                watchProgress = 60,
                isCompleted = false,
                questions = listOf(
                    Question(
                        id = "q1",
                        videoId = "vid_1",
                        questionText = "Did your child respond to their name?",
                        options = listOf("Yes", "Sometimes", "No"),
                        correctAnswerIndex = 0
                    )
                )
            ),
            Video(
                id = "vid_2",
                categoryId = "cat_detection",
                title = "Detecting Environmental Sounds",
                description = "Help your child notice common sounds.",
                duration = "4:10",
                thumbnailUrl = "",
                videoUrl = "",
                order = 2,
                isLocked = false
            ),
            Video(
                id = "vid_3",
                categoryId = "cat_discrimination",
                title = "Different Animal Sounds",
                description = "Teach your child to differentiate animal sounds.",
                duration = "5:00",
                thumbnailUrl = "",
                videoUrl = "",
                order = 1,
                isLocked = true
            )
        )
    )

     */

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
