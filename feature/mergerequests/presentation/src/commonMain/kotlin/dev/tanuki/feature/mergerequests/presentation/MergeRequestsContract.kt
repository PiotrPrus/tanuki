package dev.tanuki.feature.mergerequests.presentation

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.mergerequests.domain.MergeRequest

data class MergeRequestsState(
    val isLoading: Boolean = true,
    val mergeRequests: List<MergeRequest> = emptyList(),
    val error: UiText? = null,
)

sealed interface MergeRequestsAction {
    data object OnRefresh : MergeRequestsAction
    data class OnOpen(val mergeRequest: MergeRequest) : MergeRequestsAction
}

sealed interface MergeRequestsEvent {
    data class OpenInBrowser(val url: String) : MergeRequestsEvent
}
