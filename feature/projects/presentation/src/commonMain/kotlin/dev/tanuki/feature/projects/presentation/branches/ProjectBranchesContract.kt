package dev.tanuki.feature.projects.presentation.branches

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.Branch

data class ProjectBranchesState(
    val isLoading: Boolean = true,
    val branches: List<Branch> = emptyList(),
    val error: UiText? = null,
)

sealed interface ProjectBranchesAction {
    data object OnRetry : ProjectBranchesAction
    data class OnOpenBranch(val branch: Branch) : ProjectBranchesAction
    data class OnOpenMergeRequest(val iid: Long) : ProjectBranchesAction
}

sealed interface ProjectBranchesEvent {
    data class OpenInBrowser(val url: String) : ProjectBranchesEvent
    data class OpenMergeRequest(val projectId: Long, val iid: Long) : ProjectBranchesEvent
}
