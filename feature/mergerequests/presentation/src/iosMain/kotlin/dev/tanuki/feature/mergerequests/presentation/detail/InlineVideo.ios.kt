package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// TODO(#7): native AVPlayer inline playback. For now iOS opens the video externally.
@Composable
actual fun InlineVideo(url: String, authToken: String?, modifier: Modifier) {
    VideoFallback(url = url, modifier = modifier)
}
