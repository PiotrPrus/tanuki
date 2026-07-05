package dev.tanuki.feature.mergerequests.presentation.detail

import dev.tanuki.core.domain.diff.FileDiff
import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.mergerequests.domain.Discussion
import dev.tanuki.feature.mergerequests.domain.MergeRequest

/** A diff line the user tapped/long-pressed, identified by its side line numbers. */
data class SelectedLine(val newLine: Int?, val oldLine: Int?)

/** An in-progress multi-line selection within a single file. */
data class LineSelection(
    val newPath: String,
    val oldPath: String,
    val anchorNewLine: Int?,
    val anchorOldLine: Int?,
    val lines: Set<SelectedLine>,
)

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
    // --- discussions / review ---
    val discussions: List<Discussion> = emptyList(),
    /** Diff-anchored discussions keyed by line ("path#N<newLine>" or "path#O<oldLine>"). */
    val commentByKey: Map<String, Discussion> = emptyMap(),
    val selection: LineSelection? = null,
    val diffCommentText: String = "",
    val isPostingDiffComment: Boolean = false,
    val activeThread: Discussion? = null,
    val threadReplyText: String = "",
    val isThreadBusy: Boolean = false,
) {
    val totalAdditions: Int get() = diffs.sumOf { it.additions }
    val totalDeletions: Int get() = diffs.sumOf { it.deletions }
    val actionInProgress: Boolean get() = isApproving || isMerging || isCommenting
    val inSelectionMode: Boolean get() = selection != null
    val selectedCount: Int get() = selection?.lines?.size ?: 0
}

sealed interface MergeRequestDetailAction {
    data object OnRetry : MergeRequestDetailAction
    data object OnOpenInBrowser : MergeRequestDetailAction
    data object OnApprove : MergeRequestDetailAction
    data object OnMerge : MergeRequestDetailAction
    data class OnCommentChange(val value: String) : MergeRequestDetailAction
    data object OnSendComment : MergeRequestDetailAction

    // Line selection + diff comment
    data class OnStartSelection(val newPath: String, val oldPath: String, val newLine: Int?, val oldLine: Int?) : MergeRequestDetailAction
    data class OnToggleLine(val newPath: String, val oldPath: String, val newLine: Int?, val oldLine: Int?) : MergeRequestDetailAction
    data object OnCancelSelection : MergeRequestDetailAction
    data class OnDiffCommentChange(val value: String) : MergeRequestDetailAction
    data object OnSubmitDiffComment : MergeRequestDetailAction

    // Thread viewing
    data class OnOpenThread(val discussion: Discussion) : MergeRequestDetailAction
    data object OnCloseThread : MergeRequestDetailAction
    data class OnThreadReplyChange(val value: String) : MergeRequestDetailAction
    data object OnSubmitReply : MergeRequestDetailAction
    data class OnResolveThread(val resolved: Boolean) : MergeRequestDetailAction
}

sealed interface MergeRequestDetailEvent {
    data class OpenInBrowser(val url: String) : MergeRequestDetailEvent
    data class ShowMessage(val message: String) : MergeRequestDetailEvent
}

/** Key for a discussion's anchor line — matched against diff-line candidates in the screen. */
internal fun discussionLineKey(discussion: Discussion): String? {
    val path = discussion.filePath ?: return null
    return when {
        discussion.newLine != null -> "$path#N${discussion.newLine}"
        discussion.oldLine != null -> "$path#O${discussion.oldLine}"
        else -> null
    }
}
