package dev.tanuki.feature.auth.data.di

import dev.tanuki.feature.auth.data.AuthRepositoryImpl
import dev.tanuki.feature.auth.domain.AuthRepository
import org.koin.dsl.module

val authDataModule = module {
    single<AuthRepository> { AuthRepositoryImpl(httpClient = get(), tokenStorage = get()) }
}
