package com.example.soundforsilence.domain.repository

import com.example.soundforsilence.domain.model.Category
import com.example.soundforsilence.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun getCategories(): Flow<List<Category>>
    fun getVideosByCategory(categoryId: String): Flow<List<Video>>
    fun getVideoById(videoId: String): Flow<Video?>
    suspend fun getVideoOnce(videoId: String): Video?
    suspend fun updateVideoProgress(userId: String, videoId: String, progress: Int): Result<Unit>
    suspend fun markVideoCompleted(userId: String, videoId: String): Result<Unit>
}