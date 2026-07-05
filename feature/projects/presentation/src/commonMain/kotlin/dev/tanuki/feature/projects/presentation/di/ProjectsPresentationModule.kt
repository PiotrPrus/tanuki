package dev.tanuki.feature.projects.presentation.di

import dev.tanuki.feature.projects.presentation.ProjectsViewModel
import dev.tanuki.feature.projects.presentation.branches.ProjectBranchesViewModel
import dev.tanuki.feature.projects.presentation.dashboard.ProjectDashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val projectsPresentationModule = module {
    viewModelOf(::ProjectsViewModel)
    viewModelOf(::ProjectDashboardViewModel)
    viewModelOf(::ProjectBranchesViewModel)
}
