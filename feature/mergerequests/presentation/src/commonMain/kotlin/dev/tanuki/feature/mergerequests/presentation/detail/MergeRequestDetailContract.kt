package dev.tanuki.feature.mergerequests.presentation.detail

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.mergerequests.domain.FileDiff
import dev.tanuki.feature.mergerequests.domain.MergeRequest

data class MergeRequestDetailState(
    val isLoading: Boolean = true,
    val mergeRequest: MergeRequest? = null,
    val diffs: List<FileDiff> = emptyList(),
    val error: UiText? = null,
) {
    val totalAdditions: Int get() = diffs.sumOf { it.additions }
    val totalDeletions: Int get() = diffs.sumOf { it.deletions }
}

sealed interface MergeRequestDetailAction {
    data object OnRetry : MergeRequestDetailAction
    data object OnOpenInBrowser : MergeRequestDetailAction
}

sealed interface MergeRequestDetailEvent {
    data class OpenInBrowser(val url: String) : MergeRequestDetailEvent
}
