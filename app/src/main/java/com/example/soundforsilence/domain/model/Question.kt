package com.example.soundforsilence.domain.model

data class Question(
    val id: String = "",
    val videoId: String = "",
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0,
    val order: Int = 0
)