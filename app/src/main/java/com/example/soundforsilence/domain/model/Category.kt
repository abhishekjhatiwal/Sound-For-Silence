package com.example.soundforsilence.domain.model

data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val order: Int = 0,
    val totalVideos: Int = 0,
    val completedVideos: Int = 0
) {
    val progressPercentage: Int
        get() = if (totalVideos > 0) (completedVideos * 100) / totalVideos else 0
}