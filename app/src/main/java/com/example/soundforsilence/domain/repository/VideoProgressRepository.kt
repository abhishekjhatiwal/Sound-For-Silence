package com.example.soundforsilence.domain.repository

import com.example.soundforsilence.domain.model.VideoProgress

interface VideoProgressRepository {

    suspend fun saveProgress(userId: String, progress: VideoProgress): Result<Unit>

    suspend fun getProgressForVideo(userId: String, videoId: String): Result<VideoProgress?>

    suspend fun getProgressForUser(userId: String): Result<List<VideoProgress>>
}
