package com.example.soundforsilence.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val childName: String = "",
    val childAge: Int = 0,
    val implantDate: Long = 0,
    val therapistId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)