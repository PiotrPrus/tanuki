package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Inline video player for a GitLab upload URL.
 * Android: ExoPlayer (streams with the bearer token). iOS: opens externally for now
 * (native AVPlayer is a follow-up — see issue #7).
 */
@Composable
expect fun InlineVideo(url: String, authToken: String?, modifier: Modifier = Modifier)
