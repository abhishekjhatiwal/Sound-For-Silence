package com.example.soundforsilence.presentation.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.model.Video
import com.example.soundforsilence.domain.model.VideoUiState
import com.example.soundforsilence.domain.repository.AuthRepository
import com.example.soundforsilence.domain.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val videoId: String = checkNotNull(savedStateHandle["videoId"])

    private val _uiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    init {
        observeVideo()
    }

    private fun observeVideo() {
        videoRepository.getVideoById(videoId)
            .onEach { vid ->
                _uiState.update {
                    it.copy(
                        video = vid,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load video"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onPlayerReady(durationMs: Long) {
        _uiState.update { it.copy(durationMs = durationMs) }
    }

    fun onPositionChanged(positionMs: Long) {
        _uiState.update { it.copy(currentPositionMs = positionMs) }

        val state = _uiState.value
        val video = state.video ?: return
        val duration = state.durationMs.takeIf { it > 0 } ?: return

        // Convert to 0..100 progress
        val progress = ((positionMs.toDouble() / duration) * 100)
            .toInt()
            .coerceIn(0, 100)

        // Save progress in background
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            videoRepository.updateVideoProgress(userId, video.id, progress)
        }
    }

    fun onPlaybackCompleted() {
        val video = _uiState.value.video ?: return

        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            // Mark completed
            videoRepository.markVideoCompleted(userId, video.id)
            // Optionally, also store 100% progress explicitly
            videoRepository.updateVideoProgress(userId, video.id, 100)
        }
    }
}






















/*
@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val videoId: String = checkNotNull(savedStateHandle["videoId"])

    val video: StateFlow<Video?> =
        videoRepository.getVideoById(videoId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
}


 */