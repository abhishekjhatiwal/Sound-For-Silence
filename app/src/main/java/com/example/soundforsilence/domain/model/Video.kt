package com.example.soundforsilence.domain.model

data class Video(
    val id: String = "",
    val categoryId: String = "",
    val title: String = "",
    val description: String = "",
    val duration: String = "",
    val thumbnailUrl: String = "",
    val videoUrl: String = "",
    val order: Int = 0,
    val isLocked: Boolean = false,
    val watchProgress: Int = 0,
    val isCompleted: Boolean = false,
    val questions: List<Question> = emptyList()
)