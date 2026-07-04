package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Inline video player for a GitLab upload URL.
 * Android: ExoPlayer (falls back to the browser when the upload is session-only / private).
 * iOS: opens externally for now (native AVPlayer is a follow-up — see issue #7).
 */
@Composable
expect fun InlineVideo(url: String, authToken: String?, modifier: Modifier = Modifier)

/** Shared fallback: a tappable card that opens the video in the browser (session-authenticated). */
@Composable
fun VideoFallback(url: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Surface(
        modifier = modifier.fillMaxWidth().height(120.dp).clickable { uriHandler.openUri(url) },
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("▶ Play video", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Opens in browser",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
