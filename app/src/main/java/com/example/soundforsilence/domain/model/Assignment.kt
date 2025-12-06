package com.example.soundforsilence.domain.model

data class Assessment(
    val id: String = "",
    val userId: String = "",
    val childName: String = "",
    val period: String = "",
    val capScore: Int = 0,
    val sirScore: Int = 0,
    val assessmentDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)