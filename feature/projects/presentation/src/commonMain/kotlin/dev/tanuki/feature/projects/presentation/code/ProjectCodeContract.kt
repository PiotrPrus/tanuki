package dev.tanuki.feature.projects.presentation.code

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.Branch
import dev.tanuki.feature.projects.domain.RepoEntry

data class ProjectCodeState(
    val isLoading: Boolean = true,
    val entries: List<RepoEntry> = emptyList(),
    val error: UiText? = null,
    val branches: List<Branch> = emptyList(),
    val branchesLoading: Boolean = false,
)

sealed interface ProjectCodeAction {
    data object OnRetry : ProjectCodeAction
    data object OnLoadBranches : ProjectCodeAction
}
