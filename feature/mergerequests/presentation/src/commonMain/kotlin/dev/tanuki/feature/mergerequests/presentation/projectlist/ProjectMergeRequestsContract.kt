package dev.tanuki.feature.mergerequests.presentation.projectlist

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeRequestFilter

data class ProjectMergeRequestsState(
    val isLoading: Boolean = true,
    val filter: MergeRequestFilter = MergeRequestFilter.OPENED,
    val mergeRequests: List<MergeRequest> = emptyList(),
    val error: UiText? = null,
)

sealed interface ProjectMergeRequestsAction {
    data object OnRetry : ProjectMergeRequestsAction
    data class OnSelectFilter(val filter: MergeRequestFilter) : ProjectMergeRequestsAction
    data class OnOpen(val mergeRequest: MergeRequest) : ProjectMergeRequestsAction
}

sealed interface ProjectMergeRequestsEvent {
    data class OpenDetail(val projectId: Long, val iid: Long) : ProjectMergeRequestsEvent
}
