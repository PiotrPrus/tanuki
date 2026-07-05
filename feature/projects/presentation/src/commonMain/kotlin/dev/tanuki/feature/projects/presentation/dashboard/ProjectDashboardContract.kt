package dev.tanuki.feature.projects.presentation.dashboard

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.ProjectDetail

data class ProjectDashboardState(
    val isLoading: Boolean = true,
    val detail: ProjectDetail? = null,
    val error: UiText? = null,
)

sealed interface ProjectDashboardAction {
    data object OnRetry : ProjectDashboardAction
    data object OnOpenInBrowser : ProjectDashboardAction
}

sealed interface ProjectDashboardEvent {
    data class OpenInBrowser(val url: String) : ProjectDashboardEvent
}
