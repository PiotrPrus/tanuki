package dev.tanuki.feature.mergerequests.presentation.di

import dev.tanuki.feature.mergerequests.presentation.MergeRequestsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mergeRequestsPresentationModule = module {
    viewModelOf(::MergeRequestsViewModel)
}
