package dev.tanuki.feature.projects.presentation.pipelines

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.Pipeline

data class ProjectPipelinesState(
    val isLoading: Boolean = true,
    val pipelines: List<Pipeline> = emptyList(),
    val error: UiText? = null,
)

sealed interface ProjectPipelinesAction {
    data object OnRetry : ProjectPipelinesAction
}
