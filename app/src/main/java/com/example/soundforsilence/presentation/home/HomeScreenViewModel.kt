package com.example.soundforsilence.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.model.Category
import com.example.soundforsilence.domain.model.MostRecent
import com.example.soundforsilence.domain.model.User
import com.example.soundforsilence.domain.repository.AuthRepository
import com.example.soundforsilence.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
    authRepository: AuthRepository
) : ViewModel() {

    // -------------------------
    // 1. Categories with progress
    // -------------------------

    val categoriesWithProgress: StateFlow<List<Category>> =
        getCategoriesUseCase()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // -------------------------
    // 2. Current logged-in user
    // -------------------------

    val currentUser: StateFlow<User?> =
        authRepository.getCurrentUser()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                null
            )

    // -------------------------
    // 3. Derived stats (TEMP placeholder values)
    // -------------------------

    private val _totalVideos = MutableStateFlow(0)
    val totalVideosCount: StateFlow<Int> get() = _totalVideos

    private val _completedVideos = MutableStateFlow(0)
    val completedVideosCount: StateFlow<Int> get() = _completedVideos

    private val _overallPercent = MutableStateFlow(0)
    val overallProgressPercent: StateFlow<Int> get() = _overallPercent

    private val _streak = MutableStateFlow(0)
    val dayStreak: StateFlow<Int> get() = _streak

    // -------------------------
    // 4. Continue Watching item
    // -------------------------

    private val _mostRecentPlayed = MutableStateFlow<MostRecent?>(null)
    val mostRecent: StateFlow<MostRecent?> get() = _mostRecentPlayed



    init {
        // TEMP MOCK DATA so UI compiles & shows layout
        // Replace with Firestore later
        _totalVideos.value = 12
        _completedVideos.value = 8
        _overallPercent.value = 67
        _streak.value = 4

        _mostRecentPlayed.value = MostRecent(
            videoId = "VID123",
            title = "Learning Sound Basics",
            thumbnailUrl = null,
            progressPercent = 55,
            lastPlayedAt = System.currentTimeMillis() - 1000 * 60 * 15 // 15 min ago
        )
    }
}











/*
@HiltViewModel
class HomeViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val videoProgressRepository: VideoProgressRepository
) : ViewModel() {

    // Logged-in user
    val currentUser: StateFlow<User?> =
        authRepository.getCurrentUser()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Categories from Firestore
    val categories: StateFlow<List<Category>> =
        getCategoriesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------------------
    // 1️⃣ Fetch watch stats document live
    // ---------------------------
    val dayStreak: StateFlow<Int> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(0)
        else callbackFlow {
            val ref = firestore.collection("users")
                .document(user.id)
                .collection("meta")
                .document("stats")

            val reg = ref.addSnapshotListener { snap, _ ->
                val streak = snap?.getLong("currentStreakDays")?.toInt() ?: 0
                trySend(streak)
            }
            awaitClose { reg.remove() }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val totalWatchMs: StateFlow<Long> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(0L)
        else callbackFlow {
            val ref = firestore.collection("users")
                .document(user.id)
                .collection("meta")
                .document("stats")

            val reg = ref.addSnapshotListener { snap, _ ->
                val total = snap?.getLong("totalWatchMs") ?: 0L
                trySend(total)
            }
            awaitClose { reg.remove() }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    // ---------------------------
    // 2️⃣ Pull all video progress for user
    // ---------------------------
    val allProgress: StateFlow<List<VideoProgress>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else callbackFlow {
            val ref = firestore.collection("users")
                .document(user.id)
                .collection("videoProgress")

            val reg = ref.addSnapshotListener { snap, _ ->
                if (snap == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap.documents.map { d ->
                    VideoProgress(
                        videoId = d.getString("videoId") ?: d.id,
                        categoryId = d.getString("categoryId") ?: "",
                        positionMs = d.getLong("positionMs") ?: 0L,
                        durationMs = d.getLong("durationMs") ?: 0L,
                        completed = d.getBoolean("completed") ?: false,
                        updatedAt = d.getLong("updatedAt") ?: 0L
                    )
                }

                trySend(list)
            }

            awaitClose { reg.remove() }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---------------------------
    // 3️⃣ Compute PER-CATEGORY progress + lastPlayed
    // ---------------------------
    val categoriesWithProgress: StateFlow<List<Category>> =
        combine(categories, allProgress) { catList, progList ->

            catList.map { cat ->
                val progressForCat = progList.filter { it.categoryId == cat.id }

                val completed = progressForCat.count { it.completed }
                val lastPlayed = progressForCat.maxOfOrNull { it.updatedAt } ?: 0L

                cat.copy(
                    completedVideos = completed,
                    totalVideos = cat.totalVideos,
                    lastPlayedAt = lastPlayed
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------------------
    // 4️⃣ Global stats
    // ---------------------------
    val overallProgressPercent: StateFlow<Int> =
        categoriesWithProgress.map { list ->
            val total = list.sumOf { it.totalVideos }
            val done = list.sumOf { it.completedVideos }
            if (total == 0) 0 else ((done.toDouble() / total) * 100).toInt()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val totalVideosCount: StateFlow<Int> =
        categories.map { it.sumOf { c -> c.totalVideos } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val completedVideosCount: StateFlow<Int> =
        categoriesWithProgress.map { it.sumOf { c -> c.completedVideos } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
}


 */




