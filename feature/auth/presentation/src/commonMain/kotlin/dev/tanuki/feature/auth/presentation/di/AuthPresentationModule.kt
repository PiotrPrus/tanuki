package dev.tanuki.feature.auth.presentation.di

import dev.tanuki.feature.auth.presentation.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::LoginViewModel)
}
