package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
actual fun InlineVideo(url: String, authToken: String?, modifier: Modifier) {
    var failed by remember(url) { mutableStateOf(false) }

    if (failed) {
        // Private GitLab uploads are session-authenticated only — fall back to the browser.
        VideoFallback(url = stripAccessToken(url), modifier = modifier)
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
    AndroidView(
        factory = { ctx -> PlayerView(ctx).apply { this.player = player } },
        modifier = modifier.fillMaxWidth().height(220.dp),
    )
}

private fun stripAccessToken(url: String): String =
    url.replace(Regex("""[?&]access_token=[^&]*"""), "")
