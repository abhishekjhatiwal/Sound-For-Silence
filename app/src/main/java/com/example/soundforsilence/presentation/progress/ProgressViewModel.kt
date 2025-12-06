package com.example.soundforsilence.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.model.Assessment
import com.example.soundforsilence.domain.usecase.GetAssessmentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getAssessmentsUseCase: GetAssessmentsUseCase
) : ViewModel() {

    // For demo we use fixed userId; in real app get from AuthRepository
    private val userId = "user_001"

    val assessments: StateFlow<List<Assessment>> =
        getAssessmentsUseCase(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}
