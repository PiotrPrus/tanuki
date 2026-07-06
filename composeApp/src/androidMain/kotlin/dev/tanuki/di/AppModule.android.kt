package dev.tanuki.di

import dev.tanuki.navigation.AndroidAppLinkController
import dev.tanuki.navigation.AppLinkController
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformAppModule: Module = module {
    single<AppLinkController> { AndroidAppLinkController(androidContext()) }
}
