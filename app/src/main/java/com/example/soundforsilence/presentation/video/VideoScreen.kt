package com.example.soundforsilence.presentation.video

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.soundforsilence.domain.model.Video
import com.example.soundforsilence.domain.model.VideoUiState

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    onBack: () -> Unit,
    viewModel: VideoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val video = uiState.video
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh video data on open
    LaunchedEffect(Unit) {
        viewModel.reloadVideo()
    }

    // Debug log
    LaunchedEffect(video) {
        video?.let {
            Log.d("VIDEO_DEBUG", "Loaded video: id=${it.id}, url=${it.videoUrl}")
        }
    }

    // Save progress on lifecycle pause/stop
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.saveOnPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.saveOnPause()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(video?.title ?: "Video") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        when {
            uiState.isLoading -> LoadingBox(padding)

            uiState.error != null && video != null ->
                ErrorWithRetry(padding, uiState.error!!) {
                    viewModel.fetchFreshPlayableUrl()
                }

            video != null ->
                VideoContent(
                    video = video,
                    uiState = uiState,
                    padding = padding,
                    viewModel = viewModel
                )

            else -> EmptyVideoBox(padding)
        }
    }
}

@Composable
private fun LoadingBox(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyVideoBox(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No video selected")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun VideoContent(
    video: Video,
    uiState: VideoUiState,
    padding: PaddingValues,
    viewModel: VideoViewModel
) {
    // FIX ✔ — Only use the final resolved Cloudinary/Firestore URL
    val playableUrl = video.videoUrl.takeIf { it.isNotBlank() } ?: ""

    Log.d("VIDEO_DEBUG", "Playable URL: $playableUrl")

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {

        // Video Player
        if (playableUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
            ) {
                VideoPlayer(
                    videoUrl = playableUrl,
                    onReady = viewModel::onPlayerReady,
                    onPositionChanged = viewModel::onPositionChanged,
                    onCompleted = viewModel::onPlaybackCompleted,
                    onError = { e ->
                        viewModel.onPlayerError(e?.message ?: "Failed to load video")
                    }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f),
                contentAlignment = Alignment.Center
            ) {
                Text("No video URL available")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Video Details
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            Text(video.title, style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(4.dp))

            Text(video.description, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(8.dp))

            Text("Duration: ${video.duration}")

            Spacer(Modifier.height(8.dp))

            val percent =
                if (uiState.durationMs > 0)
                    ((uiState.currentPositionMs.toDouble() / uiState.durationMs) * 100)
                        .toInt().coerceIn(0, 100)
                else 0

            LinearProgressIndicator(progress = percent / 100f)

            Spacer(Modifier.height(4.dp))

            Text("Progress: $percent%")
        }
    }
}

@Composable
private fun ErrorWithRetry(
    padding: PaddingValues,
    msg: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(msg, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Text("Retry (get fresh URL)")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = {}) { Text("Report / Support") }
        }
    }
}





































/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    onBack: () -> Unit,
    viewModel: VideoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val video = uiState.video

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    // persist last-known position
                    viewModel.saveOnPause()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // optional final save when composable disposed (not necessary if ViewModel handles onCleared)
            viewModel.saveOnPause()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(video?.title ?: "Video") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null && video != null -> {
                // Show error + retry action to ask backend for a fresh signed URL
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "Playback error",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.fetchFreshPlayableUrl() }) {
                            Text("Retry (get fresh URL)")
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = { /* optional: open support / report flow */ }) {
                            Text("Report / Support")
                        }
                    }
                }
            }

            video != null -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    if (video.videoUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 9f)
                        ) {
                            VideoPlayer(
                                videoUrl = video.videoUrl,
                                onReady = viewModel::onPlayerReady,
                                onPositionChanged = viewModel::onPositionChanged,
                                onCompleted = viewModel::onPlaybackCompleted,
                                onError = { throwable ->
                                    viewModel.onPlayerError(
                                        throwable?.message ?: "Failed to load video"
                                    )
                                }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 9f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No video URL available")
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = video.title, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = video.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Duration: ${video.duration}",
                            style = MaterialTheme.typography.labelSmall
                        )

                        Spacer(Modifier.height(8.dp))
                        val percent =
                            if (uiState.durationMs > 0) ((uiState.currentPositionMs.toDouble() / uiState.durationMs) * 100).toInt()
                                .coerceIn(0, 100) else 0
                        LinearProgressIndicator(
                            progress = percent / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Progress: $percent%")
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No video selected")
                }
            }
        }
    }
}


 */
