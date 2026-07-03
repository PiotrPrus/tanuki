package dev.tanuki.core.data.di

import dev.tanuki.core.data.auth.SettingsTokenStorage
import dev.tanuki.core.data.network.HttpClientFactory
import dev.tanuki.core.data.network.SettingsInstanceProvider
import dev.tanuki.core.domain.auth.TokenStorage
import dev.tanuki.core.domain.config.InstanceProvider
import org.koin.core.module.Module
import org.koin.dsl.module

val coreDataModule = module {
    single<InstanceProvider> { SettingsInstanceProvider(get()) }
    single { HttpClientFactory.create(engine = get(), tokenStorage = get(), instanceProvider = get()) }
    single<TokenStorage> { SettingsTokenStorage(get()) }
}

/** Provides the platform Ktor engine and the platform-secure [com.russhwolf.settings.Settings]. */
expect val platformCoreDataModule: Module
