package dev.tanuki.core.data.di

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

@OptIn(ExperimentalSettingsImplementation::class)
actual val platformCoreDataModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single<Settings> { KeychainSettings(service = "dev.tanuki.tokens") }
}
