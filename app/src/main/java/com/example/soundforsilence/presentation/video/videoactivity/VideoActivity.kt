package com.example.soundforsilence.presentation.video.videoactivity

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.example.soundforsilence.presentation.video.VideoViewModel
import kotlin.getValue

class VideoActivity : ComponentActivity() {

    private val viewModel: VideoViewModel by viewModels()

    override fun onPause() {
        super.onPause()
        // non-blocking
        viewModel.saveOnPause()
    }
}