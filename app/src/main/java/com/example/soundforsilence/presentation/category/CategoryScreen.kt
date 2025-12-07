package com.example.soundforsilence.presentation.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.soundforsilence.domain.model.Video

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onBack: () -> Unit,
    onVideoClick: (String) -> Unit,
    viewModel: CategoryViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()
) {
    val videos by viewModel.videos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Videos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(videos) { video ->
                VideoItem(video = video) {
                    onVideoClick(video.id)
                }
            }
        }
    }
}


@Composable
fun VideoItem(video: Video, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(video.title, style = MaterialTheme.typography.titleMedium)
            Text(video.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}































/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onBack: () -> Unit,
    onVideoClick: (String) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val videos by viewModel.videos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Videos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(videos) { video ->
                VideoListItem(video = video, onClick = { onVideoClick(video.id) })
            }
        }
    }
}

@Composable
private fun VideoListItem(
    video: Video,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(video.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(video.description, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Text("Duration: ${video.duration}", style = MaterialTheme.typography.labelSmall)
        }
    }
}


 */