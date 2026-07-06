package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tanuki.core.domain.auth.TokenStorage
import dev.tanuki.core.domain.util.onFailure
import dev.tanuki.core.domain.util.onSuccess
import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.mergerequests.domain.MergeRequestRepository
import dev.tanuki.feature.mergerequests.domain.MergeRequestState
import dev.tanuki.feature.mergerequests.domain.MergeStatus
import dev.tanuki.feature.mergerequests.domain.NewDiffComment
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MergeRequestDetailViewModel(
    private val repository: MergeRequestRepository,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(MergeRequestDetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<MergeRequestDetailEvent>()
    val events = _events.receiveAsFlow()

    private var projectId: Long = 0
    private var iid: Long = 0

    fun load(projectId: Long, iid: Long) {
        this.projectId = projectId
        this.iid = iid
        reload()
    }

    fun onAction(action: MergeRequestDetailAction) {
        when (action) {
            MergeRequestDetailAction.OnRetry -> reload()
            MergeRequestDetailAction.OnOpenInBrowser -> _state.value.mergeRequest?.let { mr ->
                viewModelScope.launch { _events.send(MergeRequestDetailEvent.OpenInBrowser(mr.webUrl)) }
            }
            MergeRequestDetailAction.OnApprove -> approve()
            MergeRequestDetailAction.OnMerge -> merge()
            is MergeRequestDetailAction.OnRebase -> rebase(action.skipCi)
            is MergeRequestDetailAction.OnCommentChange ->
                _state.update { it.copy(commentText = action.value) }
            MergeRequestDetailAction.OnSendComment -> sendComment()

            is MergeRequestDetailAction.OnStartSelection -> _state.update {
                it.copy(
                    selection = LineSelection(
                        newPath = action.newPath,
                        oldPath = action.oldPath,
                        anchorNewLine = action.newLine,
                        anchorOldLine = action.oldLine,
                        lines = setOf(SelectedLine(action.newLine, action.oldLine)),
                    ),
                )
            }
            is MergeRequestDetailAction.OnToggleLine -> toggleLine(action)
            MergeRequestDetailAction.OnCancelSelection ->
                _state.update { it.copy(selection = null, diffCommentText = "") }
            is MergeRequestDetailAction.OnDiffCommentChange ->
                _state.update { it.copy(diffCommentText = action.value) }
            MergeRequestDetailAction.OnSubmitDiffComment -> submitDiffComment()

            is MergeRequestDetailAction.OnOpenThread ->
                _state.update { it.copy(activeThread = action.discussion, threadReplyText = "") }
            MergeRequestDetailAction.OnCloseThread ->
                _state.update { it.copy(activeThread = null, threadReplyText = "") }
            is MergeRequestDetailAction.OnThreadReplyChange ->
                _state.update { it.copy(threadReplyText = action.value) }
            MergeRequestDetailAction.OnSubmitReply -> submitReply()
            is MergeRequestDetailAction.OnResolveThread -> resolveThread(action.resolved)
        }
    }

    private fun toggleLine(action: MergeRequestDetailAction.OnToggleLine) {
        val current = _state.value.selection ?: return
        if (current.newPath != action.newPath) return // selection is per-file
        val line = SelectedLine(action.newLine, action.oldLine)
        val newLines = if (line in current.lines) current.lines - line else current.lines + line
        _state.update {
            it.copy(selection = if (newLines.isEmpty()) null else current.copy(lines = newLines))
        }
    }

    private fun submitDiffComment() {
        val body = _state.value.diffCommentText.trim()
        val selection = _state.value.selection ?: return
        val refs = _state.value.mergeRequest?.diffRefs
        if (body.isEmpty() || _state.value.isPostingDiffComment) return
        if (refs == null) {
            viewModelScope.launch {
                _events.send(MergeRequestDetailEvent.ShowMessage("Diff refs unavailable — can't comment on this line"))
            }
            return
        }
        _state.update { it.copy(isPostingDiffComment = true) }
        viewModelScope.launch {
            repository.addDiffComment(
                projectId = projectId,
                iid = iid,
                body = body,
                target = NewDiffComment(
                    refs = refs,
                    newPath = selection.newPath,
                    oldPath = selection.oldPath,
                    newLine = selection.anchorNewLine,
                    oldLine = if (selection.anchorNewLine == null) selection.anchorOldLine else null,
                ),
            )
                .onSuccess {
                    _state.update { it.copy(isPostingDiffComment = false, selection = null, diffCommentText = "") }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Comment posted"))
                    loadDiscussions()
                }
                .onFailure {
                    _state.update { it.copy(isPostingDiffComment = false) }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Couldn't post line comment"))
                }
        }
    }

    private fun submitReply() {
        val thread = _state.value.activeThread ?: return
        val body = _state.value.threadReplyText.trim()
        if (body.isEmpty() || _state.value.isThreadBusy) return
        _state.update { it.copy(isThreadBusy = true) }
        viewModelScope.launch {
            repository.replyToDiscussion(projectId, iid, thread.id, body)
                .onSuccess {
                    _state.update { it.copy(isThreadBusy = false, threadReplyText = "") }
                    loadDiscussions()
                }
                .onFailure {
                    _state.update { it.copy(isThreadBusy = false) }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Couldn't post reply"))
                }
        }
    }

    private fun resolveThread(resolved: Boolean) {
        val thread = _state.value.activeThread ?: return
        if (_state.value.isThreadBusy) return
        _state.update { it.copy(isThreadBusy = true) }
        viewModelScope.launch {
            repository.resolveDiscussion(projectId, iid, thread.id, resolved)
                .onSuccess {
                    _state.update { it.copy(isThreadBusy = false) }
                    _events.send(
                        MergeRequestDetailEvent.ShowMessage(if (resolved) "Thread resolved" else "Thread reopened"),
                    )
                    loadDiscussions()
                }
                .onFailure {
                    _state.update { it.copy(isThreadBusy = false) }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Couldn't update thread"))
                }
        }
    }

    private fun approve() {
        if (_state.value.actionInProgress) return
        _state.update { it.copy(isApproving = true) }
        viewModelScope.launch {
            repository.approve(projectId, iid)
                .onSuccess {
                    _state.update { it.copy(isApproving = false, approved = true) }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Approved"))
                }
                .onFailure {
                    _state.update { it.copy(isApproving = false) }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Couldn't approve this merge request"))
                }
        }
    }

    private fun merge() {
        if (_state.value.actionInProgress) return
        _state.update { it.copy(isMerging = true) }
        viewModelScope.launch {
            repository.merge(projectId, iid)
                .onSuccess {
                    _state.update { it.copy(isMerging = false) }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Merged"))
                    reload()
                }
                .onFailure {
                    _state.update { it.copy(isMerging = false) }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Couldn't merge — it may not be mergeable yet"))
                }
        }
    }

    private fun rebase(skipCi: Boolean) {
        if (_state.value.actionInProgress) return
        _state.update { it.copy(isRebasing = true) }
        viewModelScope.launch {
            repository.rebase(projectId, iid, skipCi)
                .onSuccess {
                    _events.send(MergeRequestDetailEvent.ShowMessage("Rebasing…"))
                    // GitLab rebases asynchronously — poll the MR until it finishes (or we give up).
                    var latest = _state.value.mergeRequest
                    var attempts = 0
                    while (attempts < 15) {
                        delay(2000)
                        repository.getMergeRequest(projectId, iid).onSuccess { fresh ->
                            latest = fresh
                            _state.update { s -> s.copy(mergeRequest = fresh) }
                        }
                        if (latest?.rebaseInProgress != true) break
                        attempts++
                    }
                    _state.update { it.copy(isRebasing = false) }
                    val err = latest?.mergeError
                    _events.send(
                        MergeRequestDetailEvent.ShowMessage(
                            if (err.isNullOrBlank()) "Rebased" else "Rebase failed: $err",
                        ),
                    )
                    reload()
                }
                .onFailure {
                    _state.update { it.copy(isRebasing = false) }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Couldn't start rebase"))
                }
        }
    }

    private fun sendComment() {
        val body = _state.value.commentText.trim()
        if (body.isEmpty() || _state.value.actionInProgress) return
        _state.update { it.copy(isCommenting = true) }
        viewModelScope.launch {
            repository.comment(projectId, iid, body)
                .onSuccess {
                    _state.update { it.copy(isCommenting = false, commentText = "") }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Comment posted"))
                    loadDiscussions()
                }
                .onFailure {
                    _state.update { it.copy(isCommenting = false) }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Couldn't post comment"))
                }
        }
    }

    private fun reload() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            _state.update { it.copy(accessToken = tokenStorage.getTokens()?.accessToken) }
            repository.getMergeRequest(projectId, iid)
                .onSuccess { mr -> _state.update { it.copy(mergeRequest = mr) } }
                .onFailure {
                    _state.update {
                        it.copy(isLoading = false, error = UiText.Dynamic("Couldn't load this merge request."))
                    }
                    return@launch
                }
            repository.getDiffs(projectId, iid)
                .onSuccess { diffs -> _state.update { it.copy(diffs = diffs) } }
            loadDiscussions()
            _state.update { it.copy(isLoading = false) }
            // Secondary tab data — best-effort, streams in after the main content.
            repository.getCommits(projectId, iid)
                .onSuccess { list -> _state.update { it.copy(commits = list) } }
            repository.getPipelines(projectId, iid)
                .onSuccess { list -> _state.update { it.copy(pipelines = list) } }
            repository.getApprovals(projectId, iid)
                .onSuccess { info -> _state.update { it.copy(approvals = info) } }

            // GitLab computes merge status asynchronously — an open MR often returns "checking"
            // on the first fetch. Re-fetch once shortly so the widget resolves to a real
            // status (and its Merge/Rebase button) without the user re-opening the screen.
            val mr = _state.value.mergeRequest
            if (mr != null && mr.state == MergeRequestState.OPEN && mr.status == MergeStatus.UNKNOWN) {
                delay(2500)
                repository.getMergeRequest(projectId, iid)
                    .onSuccess { fresh -> _state.update { it.copy(mergeRequest = fresh) } }
            }
        }
    }

    private suspend fun loadDiscussions() {
        repository.getDiscussions(projectId, iid).onSuccess { list ->
            val byKey = list.filter { it.isOnDiff }
                .mapNotNull { d -> discussionLineKey(d)?.let { it to d } }
                .toMap()
            _state.update { s ->
                s.copy(
                    discussions = list,
                    commentByKey = byKey,
                    activeThread = s.activeThread?.let { open -> list.find { it.id == open.id } },
                )
            }
        }
    }
}
