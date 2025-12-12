package com.example.soundforsilence.domain.model

//data class MostRecentPlayed(
//    val videoId: String,
//    val title: String,
//    val thumbnailUrl: String?,
//    val progressPercent: Int,
//    val lastPlayedAt: Long
//)

data class MostRecent(
    val videoId: String,
    val title: String,
    val thumbnailUrl: String?,
    val progressPercent: Int,
    val lastPlayedAt: Long
)
