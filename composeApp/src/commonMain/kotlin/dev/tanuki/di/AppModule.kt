package dev.tanuki.di

import dev.tanuki.core.data.di.coreDataModule
import dev.tanuki.core.data.di.platformCoreDataModule
import dev.tanuki.feature.auth.data.di.authDataModule
import dev.tanuki.feature.auth.presentation.di.authPresentationModule
import dev.tanuki.feature.mergerequests.data.di.mergeRequestsDataModule
import dev.tanuki.feature.mergerequests.presentation.di.mergeRequestsPresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            platformCoreDataModule,
            authDataModule,
            authPresentationModule,
            mergeRequestsDataModule,
            mergeRequestsPresentationModule,
        )
    }
}
