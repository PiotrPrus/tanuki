package dev.tanuki.feature.projects.presentation

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.Project
import dev.tanuki.feature.projects.domain.ProjectFilter

data class ProjectsState(
    val isLoading: Boolean = true,
    val projects: List<Project> = emptyList(),
    val filter: ProjectFilter = ProjectFilter.ALL,
    val query: String = "",
    val error: UiText? = null,
) {
    /** [projects] narrowed by the search [query] (name or path). */
    val visibleProjects: List<Project>
        get() = if (query.isBlank()) {
            projects
        } else {
            projects.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.pathWithNamespace.contains(query, ignoreCase = true)
            }
        }
}

sealed interface ProjectsAction {
    data object OnRefresh : ProjectsAction
    data class OnOpen(val project: Project) : ProjectsAction
    data class OnFilterChange(val filter: ProjectFilter) : ProjectsAction
    data class OnQueryChange(val query: String) : ProjectsAction
}

sealed interface ProjectsEvent {
    data class OpenDashboard(val projectId: Long, val projectName: String) : ProjectsEvent
}
