package com.example.soundforsilence.presentation.video.videoactivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.annotation.VisibleForTesting
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FullscreenVideoActivity : ComponentActivity() {
    private var exo: ExoPlayer? = null
    private var playerView: PlayerView? = null

    companion object {
        const val EXTRA_VIDEO_URL = "videoUrl"
        const val EXTRA_START_MS = "startMs"
        const val RESULT_POSITION_MS = "result_position_ms"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get extras
        val url = intent?.getStringExtra(EXTRA_VIDEO_URL).orEmpty()
        val startPos = intent?.getLongExtra(EXTRA_START_MS, 0L) ?: 0L

        if (url.isBlank()) {
            // Nothing to play — finish early.
            finish()
            return
        }

        // Immersive fullscreen UI
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        exo = ExoPlayer.Builder(this).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            if (startPos > 0) seekTo(startPos)
            playWhenReady = true
        }

        playerView = PlayerView(this).apply {
            player = exo
            useController = true
            // show buffering indicator always when buffering
            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }

        setContentView(playerView)
    }

    override fun onResume() {
        super.onResume()
        // resume playback if player exists
        exo?.playWhenReady = true
    }

    override fun onPause() {
        // pause playback and store current position to return to caller
        exo?.let { player ->
            val pos = player.currentPosition
            // put current position in result intent so caller can resume
            val data = Intent().apply {
                putExtra(RESULT_POSITION_MS, pos)
            }
            setResult(RESULT_OK, data)

            player.playWhenReady = false
        }
        super.onPause()
    }

    override fun onDestroy() {
        // release player
        playerView?.player = null
        exo?.release()
        exo = null
        playerView = null
        super.onDestroy()
    }

    // Exposed for tests if needed
    @VisibleForTesting
    internal fun getPlayer(): ExoPlayer? = exo
}

