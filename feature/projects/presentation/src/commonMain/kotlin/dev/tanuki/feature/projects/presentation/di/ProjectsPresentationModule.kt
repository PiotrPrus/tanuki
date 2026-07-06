package dev.tanuki.feature.projects.presentation.di

import dev.tanuki.feature.projects.presentation.ProjectsViewModel
import dev.tanuki.feature.projects.presentation.branches.ProjectBranchesViewModel
import dev.tanuki.feature.projects.presentation.code.FileViewViewModel
import dev.tanuki.feature.projects.presentation.code.ProjectCodeViewModel
import dev.tanuki.feature.projects.presentation.dashboard.ProjectDashboardViewModel
import dev.tanuki.feature.projects.presentation.groupbrowser.GroupBrowserViewModel
import dev.tanuki.feature.projects.presentation.pipelines.PipelineDetailViewModel
import dev.tanuki.feature.projects.presentation.pipelines.ProjectPipelinesViewModel
import dev.tanuki.feature.projects.presentation.refdetail.RefDetailViewModel
import dev.tanuki.feature.projects.presentation.releases.ProjectReleasesViewModel
import dev.tanuki.feature.projects.presentation.tags.ProjectTagsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val projectsPresentationModule = module {
    viewModelOf(::ProjectsViewModel)
    viewModelOf(::ProjectDashboardViewModel)
    viewModelOf(::GroupBrowserViewModel)
    viewModelOf(::ProjectBranchesViewModel)
    viewModelOf(::ProjectTagsViewModel)
    viewModelOf(::ProjectReleasesViewModel)
    viewModelOf(::RefDetailViewModel)
    viewModelOf(::ProjectPipelinesViewModel)
    viewModelOf(::PipelineDetailViewModel)
    viewModelOf(::ProjectCodeViewModel)
    viewModelOf(::FileViewViewModel)
}
