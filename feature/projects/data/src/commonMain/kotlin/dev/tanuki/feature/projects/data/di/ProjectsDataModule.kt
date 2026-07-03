package dev.tanuki.feature.projects.data.di

import dev.tanuki.feature.projects.data.ProjectRepositoryImpl
import dev.tanuki.feature.projects.domain.ProjectRepository
import org.koin.dsl.module

val projectsDataModule = module {
    single<ProjectRepository> { ProjectRepositoryImpl(httpClient = get()) }
}
