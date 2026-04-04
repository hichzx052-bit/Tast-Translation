package com.hichamdzz.translator.ui.components

import android.content.Context
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VideoBackground(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                val uri = Uri.parse("android.resource://${ctx.packageName}/raw/bg_video")
                // Try assets first
                try {
                    val afd = ctx.assets.openFd("bg_video.mp4")
                    setVideoPath(afd.fileDescriptor.toString())
                } catch (e: Exception) {
                    // fallback - will show black
                }
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    mp.setVolume(0f, 0f) // mute
                    mp.start()
                }
                setOnErrorListener { _, _, _ -> true }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
