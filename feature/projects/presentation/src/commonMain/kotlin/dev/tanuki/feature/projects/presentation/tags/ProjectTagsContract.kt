package dev.tanuki.feature.projects.presentation.tags

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.Tag

data class ProjectTagsState(
    val isLoading: Boolean = true,
    val tags: List<Tag> = emptyList(),
    val error: UiText? = null,
)

sealed interface ProjectTagsAction {
    data object OnRetry : ProjectTagsAction
}
