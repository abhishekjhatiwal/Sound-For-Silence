package com.example.soundforsilence.domain.usecase

import com.example.soundforsilence.domain.model.Video
import com.example.soundforsilence.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVideosByCategoryUseCase @Inject constructor(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(categoryId: String): Flow<List<Video>> {
        return videoRepository.getVideosByCategory(categoryId)
    }
}