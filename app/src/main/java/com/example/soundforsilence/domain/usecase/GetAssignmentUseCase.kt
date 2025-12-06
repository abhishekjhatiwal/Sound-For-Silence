package com.example.soundforsilence.domain.usecase

import com.example.soundforsilence.domain.model.Assessment
import com.example.soundforsilence.domain.repository.AssessmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAssessmentsUseCase @Inject constructor(
    private val assessmentRepository: AssessmentRepository
) {
    operator fun invoke(userId: String): Flow<List<Assessment>> {
        return assessmentRepository.getAssessmentsByUser(userId)
    }
}