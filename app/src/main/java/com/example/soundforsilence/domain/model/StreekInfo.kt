package com.example.soundforsilence.domain.model

// domain/model/StreakInfo.kt
data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: String
)

// domain/repository/StreakRepository.kt
interface StreakRepository {
    suspend fun getStreak(userId: String): Result<StreakInfo?>
    suspend fun updateStreakForToday(userId: String, today: String): Result<StreakInfo>
}
