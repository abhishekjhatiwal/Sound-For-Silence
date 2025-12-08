package com.example.soundforsilence.domain.model

data class VideoProgress(
    val videoId: String,
    val categoryId: String,
    val positionMs: Long,
    val durationMs: Long,
    val completed: Boolean,
    val updatedAt: Long
)
