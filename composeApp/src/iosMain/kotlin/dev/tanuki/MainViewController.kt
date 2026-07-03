package dev.tanuki

import androidx.compose.ui.window.ComposeUIViewController
import dev.tanuki.di.initKoin
import dev.tanuki.feature.auth.domain.OAuthRedirectHandler
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

private var koinInitialized = false

fun MainViewController(): UIViewController {
    if (!koinInitialized) {
        initKoin()
        koinInitialized = true
    }
    return ComposeUIViewController { App() }
}

/** Called from Swift `.onOpenURL` when the OAuth redirect (dev.tanuki://…) reaches the app. */
fun handleDeepLink(url: String) {
    KoinPlatform.getKoin().get<OAuthRedirectHandler>().publish(url)
}
