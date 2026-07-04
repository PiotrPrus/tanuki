package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// TODO(#7): native AVPlayer inline playback (with the bearer header on the API upload URL).
@Composable
actual fun InlineVideo(url: String, authToken: String?, aspectRatio: Float?, modifier: Modifier) {
    VideoFallback(url = url, modifier = modifier)
}
