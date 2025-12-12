package com.example.soundforsilence.presentation.video

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.model.VideoProgress
import com.example.soundforsilence.domain.model.VideoUiState
import com.example.soundforsilence.domain.repository.AuthRepository
import com.example.soundforsilence.domain.repository.VideoProgressRepository
import com.example.soundforsilence.domain.repository.VideoRepository
import com.example.soundforsilence.util.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val videoProgressRepository: VideoProgressRepository,
    private val authRepository: AuthRepository,
    private val networkUtils: NetworkUtils,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val videoId: String = checkNotNull(savedStateHandle["videoId"])

    private val _uiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    // Progress throttling
    private var lastSavedProgressPercent = -1
    private var lastSavedAtMs = 0L
    private val saveThrottleMs = 10_000L
    private val minDeltaToSavePercent = 5

    // Watch-time tracking
    private var lastPositionSnapshot = 0L
    private var accumulatedWatchMs = 0L

    init {
        debugAuth()
        observeVideoRealTime()
    }

    private fun debugAuth() {
        try {
            val u = FirebaseAuth.getInstance().currentUser
            val pid = FirebaseFirestore.getInstance().app.options.projectId
            Log.d("DBG_FIRE", "uid=${u?.uid} projectId=$pid")
        } catch (_: Throwable) { }
    }

    private fun firebaseUidOrNull(): String? =
        FirebaseAuth.getInstance().currentUser?.uid

    /**
     * 🔄 Real-time listener — used as fallback for updates.
     */
    private fun observeVideoRealTime() {
        videoRepository.getVideoById(videoId)
            .onEach { vid ->
                if (vid == null) return@onEach

                _uiState.update { it.copy(video = vid, isLoading = false, error = null) }

                if (vid.videoUrl.isNotBlank()) {
                    validateUrlOnce(vid.videoUrl)
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

    private fun validateUrlOnce(url: String) {
        viewModelScope.launch {
            val isReachable = withContext(Dispatchers.IO) {
                networkUtils.isUrlReachable(url)
            }
            if (!isReachable) {
                _uiState.update { it.copy(error = "Video URL unreachable or expired") }
            }
        }
    }

    /**
     * 🔁 Called by "Continue watching" → loads single doc immediately
     */
    fun reloadVideo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val vid = videoRepository.getVideoOnce(videoId)
                if (vid == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Video not found"
                        )
                    }
                    return@launch
                }

                Log.d("VIDEO_VM", "reloadVideo(): ${vid.id} url=${vid.videoUrl}")

                _uiState.update {
                    it.copy(video = vid, isLoading = false, error = null)
                }

                if (vid.videoUrl.isNotBlank()) validateUrlOnce(vid.videoUrl)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to reload video"
                    )
                }
            }
        }
    }

    fun onPlayerReady(durationMs: Long) {
        _uiState.update { it.copy(durationMs = durationMs) }
    }

    fun onPlayerError(msg: String) {
        _uiState.update { it.copy(error = msg) }
    }

    /**
     * 🟦 Called every ~1s by VideoPlayer
     */
    fun onPositionChanged(positionMs: Long) {
        _uiState.update { it.copy(currentPositionMs = positionMs) }

        // accumulate real watch time
        val delta = positionMs - lastPositionSnapshot
        if (delta > 0 && delta < 60_000) accumulatedWatchMs += delta
        lastPositionSnapshot = positionMs

        val state = _uiState.value
        val video = state.video ?: return
        val duration = state.durationMs.takeIf { it > 0 } ?: return

        val pct = ((positionMs.toDouble() / duration) * 100).toInt().coerceIn(0, 100)
        val now = System.currentTimeMillis()

        val shouldSave = when {
            pct >= 100 -> true
            lastSavedProgressPercent < 0 -> true
            now - lastSavedAtMs >= saveThrottleMs -> true
            abs(pct - lastSavedProgressPercent) >= minDeltaToSavePercent -> true
            else -> false
        }
        if (!shouldSave) return

        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch

            val progress = VideoProgress(
                videoId = video.id,
                categoryId = video.categoryId,
                positionMs = positionMs,
                durationMs = duration,
                completed = pct >= 100,
                updatedAt = System.currentTimeMillis()
            )

            val result = videoProgressRepository.saveProgress(uid, progress)
            if (result.isSuccess) {
                lastSavedProgressPercent = pct
                lastSavedAtMs = now
            } else {
                _uiState.update {
                    it.copy(error = result.exceptionOrNull()?.message ?: "Failed to save progress")
                }
            }
        }
    }

    /**
     * ▶️ Playback completed → persist as complete
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onPlaybackCompleted() {
        val video = _uiState.value.video ?: return
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch

            val dur = _uiState.value.durationMs
            val progress = VideoProgress(
                videoId = video.id,
                categoryId = video.categoryId,
                positionMs = dur,
                durationMs = dur,
                completed = true,
                updatedAt = System.currentTimeMillis()
            )

            val res = videoProgressRepository.saveProgress(userId, progress)
            if (res.isSuccess) {
                _uiState.update { it.copy(currentPositionMs = dur) }
            } else {
                _uiState.update {
                    it.copy(
                        error = res.exceptionOrNull()?.message ?: "Failed to mark completed"
                    )
                }
            }

            persistWatchStatsIfNeeded()
        }
    }

    /**
     * 🔁 Retry Cloudinary link check
     */
    fun fetchFreshPlayableUrl() {
        val video = _uiState.value.video ?: return
        val url = video.videoUrl

        if (url.isBlank()) {
            _uiState.update { it.copy(error = "No video URL available") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(error = "Checking video URL...") }

            val reachable = try {
                withContext(Dispatchers.IO) { networkUtils.isUrlReachable(url) }
            } catch (_: Exception) {
                false
            }

            if (reachable) {
                _uiState.update { it.copy(error = null) }
            } else {
                _uiState.update { it.copy(error = "Video URL unreachable or expired") }
            }
        }
    }

    /**
     * 📊 Watch stat tracker
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun persistWatchStatsIfNeeded() {
        val uid = authRepository.getCurrentUserId() ?: return
        if (accumulatedWatchMs <= 0) return

        val fs = FirebaseFirestore.getInstance()
        val statsRef = fs.collection("users")
            .document(uid)
            .collection("meta")
            .document("stats")

        try {
            fs.runTransaction { tx ->
                val snap = tx.get(statsRef)

                val prevTotal = snap.getLong("totalWatchMs") ?: 0L
                val lastDate = snap.getString("lastWatchDate") ?: ""
                var streak = snap.getLong("currentStreakDays")?.toInt() ?: 0

                val today = java.time.LocalDate.now().toString()
                val yesterday = java.time.LocalDate.now().minusDays(1).toString()

                streak = if (lastDate == yesterday) streak + 1 else 1

                tx.set(
                    statsRef,
                    mapOf(
                        "totalWatchMs" to (prevTotal + accumulatedWatchMs),
                        "lastWatchDate" to today,
                        "currentStreakDays" to streak
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }.await()

            accumulatedWatchMs = 0L

        } catch (e: Exception) {
            Log.e("VideoVM", "persistWatchStatsIfNeeded failed", e)
            _uiState.update { it.copy(error = e.message ?: "Failed to save watch stats") }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCleared() {
        try {
            saveOnPause()
        } catch (_: Throwable) { }
        super.onCleared()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveOnPause() {
        viewModelScope.launch {
            val state = _uiState.value
            val video = state.video ?: return@launch

            val uid = authRepository.getCurrentUserId() ?: return@launch

            val progress = VideoProgress(
                videoId = video.id,
                categoryId = video.categoryId,
                positionMs = state.currentPositionMs,
                durationMs = state.durationMs,
                completed = false,
                updatedAt = System.currentTimeMillis()
            )

            videoProgressRepository.saveProgress(uid, progress)
            persistWatchStatsIfNeeded()
        }
    }
}
