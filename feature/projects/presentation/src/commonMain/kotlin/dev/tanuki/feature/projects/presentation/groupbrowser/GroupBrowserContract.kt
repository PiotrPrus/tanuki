package dev.tanuki.feature.projects.presentation.groupbrowser

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.Group
import dev.tanuki.feature.projects.domain.Project

data class GroupBrowserState(
    val isLoading: Boolean = true,
    val title: String = "",
    val fullPath: String = "",
    val subgroups: List<Group> = emptyList(),
    val projects: List<Project> = emptyList(),
    val error: UiText? = null,
)

sealed interface GroupBrowserAction {
    data object OnRetry : GroupBrowserAction
    data class OnOpenSubgroup(val group: Group) : GroupBrowserAction
    data class OnOpenProject(val project: Project) : GroupBrowserAction
    data class OnToggleStar(val project: Project) : GroupBrowserAction
    /** A breadcrumb ancestor segment was tapped. */
    data class OnOpenGroupPath(val fullPath: String) : GroupBrowserAction
}

sealed interface GroupBrowserEvent {
    data class OpenGroup(val fullPath: String) : GroupBrowserEvent
    data class OpenDashboard(val projectId: Long, val projectName: String) : GroupBrowserEvent
}
