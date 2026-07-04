package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

// TODO(#7): native AVPlayer inline playback. For now iOS opens the video externally.
@Composable
actual fun InlineVideo(url: String, authToken: String?, modifier: Modifier) {
    val uriHandler = LocalUriHandler.current
    Surface(
        modifier = modifier.fillMaxWidth().height(160.dp).clickable { uriHandler.openUri(url) },
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("▶ Play video", style = MaterialTheme.typography.titleMedium)
        }
    }
}
