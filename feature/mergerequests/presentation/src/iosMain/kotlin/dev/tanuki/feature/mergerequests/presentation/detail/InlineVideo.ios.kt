package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVURLAsset
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun InlineVideo(url: String, authToken: String?, aspectRatio: Float?, modifier: Modifier) {
    val controller = remember(url, authToken) {
        // "AVURLAssetHTTPHeaderFieldsKey" isn't exposed as a K/N constant; use the literal key.
        val options: Map<Any?, *> = if (authToken != null) {
            mapOf<Any?, Any?>(
                "AVURLAssetHTTPHeaderFieldsKey" to mapOf("Authorization" to "Bearer $authToken"),
            )
        } else {
            emptyMap<Any?, Any?>()
        }
        val asset = AVURLAsset(uRL = NSURL(string = url)!!, options = options)
        AVPlayerViewController().apply {
            player = AVPlayer(playerItem = AVPlayerItem(asset = asset))
        }
    }
    val frame = when {
        aspectRatio == null -> Modifier.fillMaxWidth().aspectRatio(16f / 9f)
        aspectRatio < 1f -> Modifier.height(460.dp).aspectRatio(aspectRatio)
        else -> Modifier.fillMaxWidth().aspectRatio(aspectRatio)
    }
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        UIKitView(factory = { controller.view }, modifier = frame)
    }
}
