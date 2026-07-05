package dev.tanuki.feature.projects.presentation.releases

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.Release

data class ProjectReleasesState(
    val isLoading: Boolean = true,
    val releases: List<Release> = emptyList(),
    val error: UiText? = null,
)

sealed interface ProjectReleasesAction {
    data object OnRetry : ProjectReleasesAction
}
