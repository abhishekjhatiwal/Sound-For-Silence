package com.example.soundforsilence.domain.repository

import com.example.soundforsilence.domain.model.StreakInfo

interface StreakRepository {
    suspend fun getStreak(userId: String): Result<StreakInfo?>
    suspend fun updateStreakForToday(userId: String, today: String): Result<StreakInfo>
}