package dev.tanuki.feature.mergerequests.data.di

import dev.tanuki.feature.mergerequests.data.MergeRequestRepositoryImpl
import dev.tanuki.feature.mergerequests.domain.MergeRequestRepository
import org.koin.dsl.module

val mergeRequestsDataModule = module {
    single<MergeRequestRepository> { MergeRequestRepositoryImpl(httpClient = get()) }
}
