package com.example.soundforsilence.domain.repository

import com.example.soundforsilence.domain.model.Assessment
import kotlinx.coroutines.flow.Flow

interface AssessmentRepository {
    fun getAssessmentsByUser(userId: String): Flow<List<Assessment>>
    suspend fun addAssessment(assessment: Assessment): Result<Unit>
}