package com.hichamdzz.updatemanager.ui.components

import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VideoBackground(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(Unit) { onDispose { mediaPlayer?.release() } }
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        try {
                            val mp = MediaPlayer()
                            val afd: AssetFileDescriptor = ctx.assets.openFd("bg_video.mp4")
                            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                            mp.setSurface(holder.surface)
                            mp.isLooping = true
                            mp.setVolume(0f, 0f)
                            mp.setOnPreparedListener { it.start() }
                            mp.prepareAsync()
                            mediaPlayer = mp
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                    override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, he: Int) {}
                    override fun surfaceDestroyed(h: SurfaceHolder) { mediaPlayer?.release(); mediaPlayer = null }
                })
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
