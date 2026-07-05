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
    data class OnOpen(val release: Release) : ProjectReleasesAction
}

sealed interface ProjectReleasesEvent {
    data class OpenInBrowser(val url: String) : ProjectReleasesEvent
}
