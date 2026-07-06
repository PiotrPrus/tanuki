package dev.tanuki.di

import dev.tanuki.navigation.AppLinkController
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS has no user-gated link toggle (Universal Links either verify or don't), so the prompt never shows. */
private class IosAppLinkController : AppLinkController {
    override fun areGitLabLinksEnabled(): Boolean = true
    override fun openLinkSettings() = Unit
}

actual val platformAppModule: Module = module {
    single<AppLinkController> { IosAppLinkController() }
}
