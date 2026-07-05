package dev.tanuki.feature.projects.presentation.pipelines

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.PipelineJob

data class PipelineDetailState(
    val title: String = "",
    val isLoading: Boolean = true,
    val jobs: List<PipelineJob> = emptyList(),
    val error: UiText? = null,
)

sealed interface PipelineDetailAction {
    data object OnRetry : PipelineDetailAction
    data class OnOpenJob(val job: PipelineJob) : PipelineDetailAction
}

sealed interface PipelineDetailEvent {
    data class OpenInBrowser(val url: String) : PipelineDetailEvent
}
