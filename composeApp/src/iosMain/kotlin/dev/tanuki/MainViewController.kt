package dev.tanuki

import androidx.compose.ui.window.ComposeUIViewController
import dev.tanuki.di.initKoin
import platform.UIKit.UIViewController

private var koinInitialized = false

fun MainViewController(): UIViewController {
    if (!koinInitialized) {
        initKoin()
        koinInitialized = true
    }
    return ComposeUIViewController { App() }
}
