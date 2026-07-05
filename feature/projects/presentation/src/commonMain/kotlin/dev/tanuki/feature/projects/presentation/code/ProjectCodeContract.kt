package dev.tanuki.feature.projects.presentation.code

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.RepoEntry

data class ProjectCodeState(
    val isLoading: Boolean = true,
    val entries: List<RepoEntry> = emptyList(),
    val error: UiText? = null,
)

sealed interface ProjectCodeAction {
    data object OnRetry : ProjectCodeAction
}
