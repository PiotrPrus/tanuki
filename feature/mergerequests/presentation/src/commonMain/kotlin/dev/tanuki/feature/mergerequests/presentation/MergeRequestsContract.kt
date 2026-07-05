package dev.tanuki.feature.mergerequests.presentation

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.mergerequests.domain.MergeRequest

/** Which "my work" slice of open MRs the Reviews tab is showing. */
enum class ReviewScope(val label: String) {
    ASSIGNED_TO_ME("Assigned to me"),
    REVIEW_REQUESTED("Review requested"),
}

data class MergeRequestsState(
    val isLoading: Boolean = true,
    val scope: ReviewScope = ReviewScope.ASSIGNED_TO_ME,
    val mergeRequests: List<MergeRequest> = emptyList(),
    val error: UiText? = null,
)

sealed interface MergeRequestsAction {
    data object OnRefresh : MergeRequestsAction
    data class OnSelectScope(val scope: ReviewScope) : MergeRequestsAction
    data class OnOpen(val mergeRequest: MergeRequest) : MergeRequestsAction
}

sealed interface MergeRequestsEvent {
    data class OpenDetail(val projectId: Long, val iid: Long) : MergeRequestsEvent
}
