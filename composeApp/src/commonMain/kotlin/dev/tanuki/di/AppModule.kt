package dev.tanuki.di

import dev.tanuki.core.data.di.coreDataModule
import dev.tanuki.core.data.di.platformCoreDataModule
import dev.tanuki.feature.auth.data.di.authDataModule
import dev.tanuki.feature.auth.presentation.di.authPresentationModule
import dev.tanuki.feature.mergerequests.data.di.mergeRequestsDataModule
import dev.tanuki.feature.mergerequests.presentation.di.mergeRequestsPresentationModule
import dev.tanuki.feature.projects.data.di.projectsDataModule
import dev.tanuki.feature.projects.presentation.di.projectsPresentationModule
import dev.tanuki.navigation.DeepLinkHandler
import dev.tanuki.navigation.DefaultDeepLinkHandler
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<DeepLinkHandler> { DefaultDeepLinkHandler() }
}

/** Provides [dev.tanuki.navigation.AppLinkController] — the platform "open supported links" hook. */
expect val platformAppModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            appModule,
            platformAppModule,
            coreDataModule,
            platformCoreDataModule,
            authDataModule,
            authPresentationModule,
            projectsDataModule,
            projectsPresentationModule,
            mergeRequestsDataModule,
            mergeRequestsPresentationModule,
        )
    }
}
