package com.example.soundforsilence.presentation.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.model.Video
import com.example.soundforsilence.domain.usecase.GetVideosByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getVideosByCategoryUseCase: GetVideosByCategoryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String = checkNotNull(savedStateHandle["categoryId"])

    val videos: StateFlow<List<Video>> =
        getVideosByCategoryUseCase(categoryId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}
