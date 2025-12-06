package com.example.soundforsilence.domain.usecase

import com.example.soundforsilence.domain.model.Category
import com.example.soundforsilence.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(): Flow<List<Category>> {
        return videoRepository.getCategories()
    }
}