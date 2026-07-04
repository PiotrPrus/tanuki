package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
actual fun InlineVideo(url: String, authToken: String?, aspectRatio: Float?, modifier: Modifier) {
    var failed by remember(url) { mutableStateOf(false) }

    if (failed) {
        VideoFallback(url = url, modifier = modifier)
        return
    }

    val context = LocalContext.current
    val player = remember(url, authToken) {
        val httpFactory = DefaultHttpDataSource.Factory().apply {
            if (authToken != null) {
                setDefaultRequestProperties(mapOf("Authorization" to "Bearer $authToken"))
            }
        }
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(url))
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        failed = true
                    }
                })
                prepare()
            }
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }

    // Portrait clips (aspect < 1) get a tall, centered frame; landscape fills width.
    val frame = when {
        aspectRatio == null -> Modifier.fillMaxWidth().aspectRatio(16f / 9f)
        aspectRatio < 1f -> Modifier.height(460.dp).aspectRatio(aspectRatio)
        else -> Modifier.fillMaxWidth().aspectRatio(aspectRatio)
    }
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { this.player = player } },
            modifier = frame,
        )
    }
}
