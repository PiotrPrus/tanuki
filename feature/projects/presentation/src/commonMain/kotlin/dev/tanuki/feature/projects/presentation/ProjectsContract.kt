package dev.tanuki.feature.projects.presentation

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.Project

data class ProjectsState(
    val isLoading: Boolean = true,
    val projects: List<Project> = emptyList(),
    val error: UiText? = null,
)

sealed interface ProjectsAction {
    data object OnRefresh : ProjectsAction
    data class OnOpen(val project: Project) : ProjectsAction
}

sealed interface ProjectsEvent {
    data class OpenInBrowser(val url: String) : ProjectsEvent
}
