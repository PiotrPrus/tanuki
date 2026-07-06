package dev.tanuki.feature.projects.presentation

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.Group
import dev.tanuki.feature.projects.domain.Project

data class ProjectsState(
    val isLoading: Boolean = true,
    /** Top-level groups (shown when not searching). */
    val groups: List<Group> = emptyList(),
    /** The user's personal-namespace projects (shown when not searching). */
    val personalProjects: List<Project> = emptyList(),
    /** Flat search results across all projects (shown when [query] is non-blank). */
    val searchResults: List<Project> = emptyList(),
    val query: String = "",
    val isSearching: Boolean = false,
    val error: UiText? = null,
) {
    val searchMode: Boolean get() = query.isNotBlank()
}

sealed interface ProjectsAction {
    data object OnRefresh : ProjectsAction
    data class OnQueryChange(val query: String) : ProjectsAction
    data class OnOpenProject(val project: Project) : ProjectsAction
    data class OnOpenGroup(val group: Group) : ProjectsAction
    data class OnToggleStar(val project: Project) : ProjectsAction
}

sealed interface ProjectsEvent {
    data class OpenDashboard(val projectId: Long, val projectName: String) : ProjectsEvent
    data class OpenGroup(val fullPath: String, val name: String) : ProjectsEvent
}
