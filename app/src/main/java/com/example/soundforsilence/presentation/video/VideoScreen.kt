package com.example.soundforsilence.presentation.video

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

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

