package com.example.soundforsilence.presentation.video

import android.content.Intent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.soundforsilence.presentation.video.videoactivity.FullscreenVideoActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    onReady: (durationMs: Long) -> Unit = {},
    onPositionChanged: (positionMs: Long) -> Unit = {},
    onCompleted: () -> Unit = {},
    onError: (throwable: Throwable?) -> Unit = {}
) {
    val context = LocalContext.current

    // remember single ExoPlayer instance per URL
    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = autoPlay
        }
    }

    // buffering state
    val isBuffering = remember { mutableStateOf(false) }

    // add/remove listener & release on dispose
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> isBuffering.value = true
                    Player.STATE_READY -> {
                        isBuffering.value = false
                        val duration = exoPlayer.duration
                        if (duration > 0L) onReady(duration)
                    }
                    Player.STATE_ENDED -> onCompleted()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                onError(error)
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // periodic position reporter
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            try {
                if (exoPlayer.playbackState == Player.STATE_READY || exoPlayer.isPlaying) {
                    onPositionChanged(exoPlayer.currentPosition)
                }
            } catch (_: Exception) { /* ignore */ }
            delay(1000L)
        }
    }

    // UI
    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = true
                }
            }
        )

        // Buffering spinner (center)
        if (isBuffering.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Fullscreen button (top-right)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = {
                    val intent = Intent(context, FullscreenVideoActivity::class.java).apply {
                        putExtra(FullscreenVideoActivity.EXTRA_VIDEO_URL, videoUrl)
                        putExtra(FullscreenVideoActivity.EXTRA_START_MS, exoPlayer.currentPosition)
                    }
                    context.startActivity(intent)
                }
            ) {
                Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen")
            }
        }
    }
}
