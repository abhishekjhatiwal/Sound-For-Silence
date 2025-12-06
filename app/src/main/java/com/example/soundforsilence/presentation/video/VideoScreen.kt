package com.example.soundforsilence.presentation.video

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    onBack: () -> Unit,
    viewModel: VideoViewModel = hiltViewModel()
) {
    val video by viewModel.video.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(video?.title ?: "Video") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        video?.let { vid ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(vid.title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(vid.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text("Duration: ${vid.duration}", style = MaterialTheme.typography.labelSmall)

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { /* TODO: start video player */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Play Video")
                }
            }
        } ?: Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
