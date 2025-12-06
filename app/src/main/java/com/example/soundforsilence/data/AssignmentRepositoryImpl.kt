package com.example.soundforsilence.data

import com.example.soundforsilence.domain.model.Assessment
import com.example.soundforsilence.domain.repository.AssessmentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class AssessmentRepositoryImpl : AssessmentRepository {

    private val assessmentsFlow = MutableStateFlow<List<Assessment>>(emptyList())

    init {
        // sample data
        assessmentsFlow.value = listOf(
            Assessment(
                id = "assess_1",
                userId = "user_001",
                childName = "Child Name",
                period = "Week 1",
                capScore = 65,
                sirScore = 70,
                notes = "Good progress in detection."
            ),
            Assessment(
                id = "assess_2",
                userId = "user_001",
                childName = "Child Name",
                period = "Week 2",
                capScore = 72,
                sirScore = 75,
                notes = "Better attention to environmental sounds."
            )
        )
    }

    override fun getAssessmentsByUser(userId: String): Flow<List<Assessment>> =
        assessmentsFlow.map { list -> list.filter { it.userId == userId } }

    override suspend fun addAssessment(assessment: Assessment): Result<Unit> {
        delay(200)
        val list = assessmentsFlow.value.toMutableList()
        list.add(assessment)
        assessmentsFlow.value = list
        return Result.success(Unit)
    }
}
