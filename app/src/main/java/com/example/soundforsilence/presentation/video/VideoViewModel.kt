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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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

    // Throttle state for writes
    private var lastSavedProgressPercent: Int = -1
    private var lastSavedAtMs: Long = 0L
    private val saveThrottleMs = 10_000L
    private val minDeltaToSavePercent = 5

    // Watch-time tracking
    private var lastPositionSnapshot: Long = 0L
    private var accumulatedWatchMs: Long = 0L

    init {
        // Log current auth / project info for debugging (safe: not sensitive)
        try {
            val fbAuth = FirebaseAuth.getInstance()
            val uid = fbAuth.currentUser?.uid
            val projectId = FirebaseFirestore.getInstance().app.options.projectId
            Log.d("DBG_FIRE", "currentUser.uid = $uid  | projectId = $projectId")
        } catch (t: Throwable) {
            Log.w("DBG_FIRE", "unable to log firebase info", t)
        }

        observeVideo()
    }

    private fun observeVideo() {
        videoRepository.getVideoById(videoId)
            .onEach { vid ->
                _uiState.update { it.copy(video = vid, isLoading = false, error = null) }

                // Optionally validate reachability once
                vid?.videoUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    viewModelScope.launch {
                        val reachable =
                            withContext(Dispatchers.IO) { networkUtils.isUrlReachable(url) }
                        if (!reachable) _uiState.update { it.copy(error = "Video URL unreachable or expired") }
                    }
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

    fun onPlayerError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCleared() {
        // ensure last position & stats are saved when ViewModel is destroyed
        // fire-and-forget because ViewModel is being cleared
        try {
            saveOnPause()
        } catch (t: Throwable) {
            Log.w("VideoViewModel", "saveOnPause failed in onCleared", t)
        }
        super.onCleared()
    }

    /**
     * Public helper: immediately persist the current UI position/duration to backend.
     * Safe to call from Activity.onPause(), player pause, or Compose lifecycle observer.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveOnPause() {
        viewModelScope.launch {
            val state = _uiState.value
            val video = state.video ?: return@launch
            val position = state.currentPositionMs
            val duration = state.durationMs.takeIf { it > 0 } ?: 0L

            val userId = authRepository.getCurrentUserId()
            Log.d(
                "VideoViewModel",
                "saveOnPause -> currentUserId = $userId, video=${video.id}, pos=$position"
            )
            if (userId == null) {
                _uiState.update { it.copy(error = "Not signed in — progress not saved") }
                return@launch
            }

            val progress = VideoProgress(
                videoId = video.id,
                categoryId = video.categoryId,
                positionMs = position,
                durationMs = duration,
                completed = (if (duration > 0) (position >= duration) else false),
                updatedAt = System.currentTimeMillis()
            )

            try {
                val res = videoProgressRepository.saveProgress(userId, progress)
                if (res.isFailure) {
                    _uiState.update {
                        it.copy(
                            error = res.exceptionOrNull()?.message
                                ?: "Failed to save progress on pause"
                        )
                    }
                } else {
                    lastSavedAtMs = System.currentTimeMillis()
                    lastSavedProgressPercent =
                        if (duration > 0) ((position.toDouble() / duration) * 100).toInt()
                            .coerceIn(0, 100) else lastSavedProgressPercent
                }
            } catch (e: Exception) {
                Log.e("VideoViewModel", "saveOnPause error", e)
                _uiState.update { it.copy(error = e.message ?: "Failed to save progress on pause") }
            }

            // persist accumulated watch-time
            try {
                persistWatchStatsIfNeeded()
            } catch (e: Exception) {
                Log.w("VideoViewModel", "persistWatchStatsIfNeeded failed", e)
            }
        }
    }

    /**
     * Called frequently (player -> every 1s). Updates UI immediately.
     * Persists via VideoProgressRepository.saveProgress with (positionMs, durationMs).
     * Also tracks accumulated watch time.
     */
    fun onPositionChanged(positionMs: Long) {
        _uiState.update { it.copy(currentPositionMs = positionMs) }

        // accumulate watch time (sanity check)
        val delta = positionMs - lastPositionSnapshot
        if (delta > 0 && delta < 60_000) { // only count reasonable increments
            accumulatedWatchMs += delta
        }
        lastPositionSnapshot = positionMs

        val state = _uiState.value
        val video = state.video ?: return
        val duration = state.durationMs.takeIf { it > 0 } ?: return

        val progressPercent = ((positionMs.toDouble() / duration) * 100).toInt().coerceIn(0, 100)
        val now = System.currentTimeMillis()

        val shouldSave = when {
            progressPercent >= 100 -> true // always persist completion
            lastSavedProgressPercent < 0 -> true // first save
            (now - lastSavedAtMs) >= saveThrottleMs -> true
            abs(progressPercent - lastSavedProgressPercent) >= minDeltaToSavePercent -> true
            else -> false
        }

        if (!shouldSave) return

        // Persist VideoProgress (real ms values)
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: run {
                Log.w("VideoViewModel", "onPositionChanged: no signed-in user")
                return@launch
            }

            val progress = VideoProgress(
                videoId = video.id,
                categoryId = video.categoryId,
                positionMs = positionMs,
                durationMs = duration,
                completed = progressPercent >= 100,
                updatedAt = System.currentTimeMillis()
            )

            try {
                val res = videoProgressRepository.saveProgress(userId, progress)
                if (res.isSuccess) {
                    lastSavedProgressPercent = progressPercent
                    lastSavedAtMs = System.currentTimeMillis()
                } else {
                    // store non-fatal error in UI
                    _uiState.update {
                        it.copy(
                            error = res.exceptionOrNull()?.message ?: "Failed to save progress"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoViewModel", "onPositionChanged save failed", e)
                _uiState.update { it.copy(error = e.message ?: "Failed to save progress") }
            }
        }
    }

    /**
     * Called when playback ends — writes final progress with position=duration and completed=true.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onPlaybackCompleted() {
        val video = _uiState.value.video ?: return
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            Log.d(
                "VideoViewModel",
                "onPlaybackCompleted -> currentUserId = $userId, video=${video.id}"
            )
            if (userId == null) {
                _uiState.update { it.copy(error = "Not signed in — cannot mark complete") }
                return@launch
            }

            val duration = _uiState.value.durationMs
            val progress = VideoProgress(
                videoId = video.id,
                categoryId = video.categoryId,
                positionMs = duration,
                durationMs = duration,
                completed = true,
                updatedAt = System.currentTimeMillis()
            )

            try {
                val res = videoProgressRepository.saveProgress(userId, progress)
                if (res.isSuccess) {
                    _uiState.update { it.copy(currentPositionMs = duration) }
                } else {
                    _uiState.update {
                        it.copy(
                            error = res.exceptionOrNull()?.message ?: "Failed to mark completed"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoViewModel", "onPlaybackCompleted error", e)
                _uiState.update { it.copy(error = e.message ?: "Failed to mark completed") }
            }

            // flush watch stats
            try {
                persistWatchStatsIfNeeded()
            } catch (e: Exception) {
                Log.w("VideoViewModel", "persistWatchStatsIfNeeded failed", e)
            }
        }
    }

    /**
     * Try to "refresh" playback by re-checking the current video's URL reachability.
     */
    fun fetchFreshPlayableUrl() {
        val video = _uiState.value.video ?: run {
            _uiState.update { it.copy(error = "No video to refresh") }
            return
        }

        val url = video.videoUrl
        if (url.isBlank()) {
            _uiState.update { it.copy(error = "No video URL available") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(error = "Checking video URL...") }
            val reachable = try {
                withContext(Dispatchers.IO) { networkUtils.isUrlReachable(url) }
            } catch (e: Exception) {
                false
            }

            if (reachable) {
                // clear the error so UI can re-render and player can try again
                _uiState.update { it.copy(error = null) }
            } else {
                _uiState.update { it.copy(error = "Video URL unreachable or expired") }
            }
        }
    }

    // called on pause / onCleared
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun persistWatchStatsIfNeeded() {
        val uid = authRepository.getCurrentUserId() ?: run {
            Log.w("VideoViewModel", "persistWatchStatsIfNeeded: no signed-in user")
            return
        }
        if (accumulatedWatchMs <= 0) return

        val firestore = FirebaseFirestore.getInstance()
        val statRef =
            firestore.collection("users").document(uid).collection("meta").document("stats")

        // read existing values and update in a transaction
        try {
            firestore.runTransaction { tx ->
                val snap = tx.get(statRef)
                val prevTotal = snap.getLong("totalWatchMs") ?: 0L
                val lastDate = snap.getString("lastWatchDate") ?: ""
                var streak = (snap.getLong("currentStreakDays") ?: 0L).toInt()
                val newTotal = prevTotal + accumulatedWatchMs
                val today = java.time.LocalDate.now().toString()
                if (lastDate != today) {
                    val yesterday = java.time.LocalDate.now().minusDays(1).toString()
                    streak = if (lastDate == yesterday) streak + 1 else 1
                }
                tx.set(
                    statRef, mapOf(
                        "totalWatchMs" to newTotal,
                        "lastWatchDate" to today,
                        "currentStreakDays" to streak
                    ), com.google.firebase.firestore.SetOptions.merge()
                )
            }.await()
            accumulatedWatchMs = 0L
        } catch (e: Exception) {
            Log.e("VideoViewModel", "persistWatchStatsIfNeeded failed", e)
            _uiState.update { it.copy(error = e.message ?: "Failed to save watch stats") }
        }
    }
}

