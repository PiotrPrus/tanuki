package dev.tanuki.feature.mergerequests.presentation.detail

import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.mergerequests.domain.FileDiff
import dev.tanuki.feature.mergerequests.domain.MergeRequest

data class MergeRequestDetailState(
    val isLoading: Boolean = true,
    val mergeRequest: MergeRequest? = null,
    val diffs: List<FileDiff> = emptyList(),
    val error: UiText? = null,
    val commentText: String = "",
    val approved: Boolean = false,
    val isApproving: Boolean = false,
    val isMerging: Boolean = false,
    val isCommenting: Boolean = false,
    /** Access token for authenticated media (video) streaming. */
    val accessToken: String? = null,
) {
    val totalAdditions: Int get() = diffs.sumOf { it.additions }
    val totalDeletions: Int get() = diffs.sumOf { it.deletions }
    val actionInProgress: Boolean get() = isApproving || isMerging || isCommenting
}

sealed interface MergeRequestDetailAction {
    data object OnRetry : MergeRequestDetailAction
    data object OnOpenInBrowser : MergeRequestDetailAction
    data object OnApprove : MergeRequestDetailAction
    data object OnMerge : MergeRequestDetailAction
    data class OnCommentChange(val value: String) : MergeRequestDetailAction
    data object OnSendComment : MergeRequestDetailAction
}

sealed interface MergeRequestDetailEvent {
    data class OpenInBrowser(val url: String) : MergeRequestDetailEvent
    data class ShowMessage(val message: String) : MergeRequestDetailEvent
}
