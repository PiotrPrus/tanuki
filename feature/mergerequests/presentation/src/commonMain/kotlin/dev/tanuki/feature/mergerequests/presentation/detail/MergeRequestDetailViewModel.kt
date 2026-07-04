package dev.tanuki.feature.mergerequests.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tanuki.core.domain.util.onFailure
import dev.tanuki.core.domain.util.onSuccess
import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.mergerequests.domain.MergeRequestRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MergeRequestDetailViewModel(
    private val repository: MergeRequestRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MergeRequestDetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<MergeRequestDetailEvent>()
    val events = _events.receiveAsFlow()

    private var projectId: Long = 0
    private var iid: Long = 0

    /** Called once by the screen with the navigation arguments. */
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
            is MergeRequestDetailAction.OnCommentChange ->
                _state.update { it.copy(commentText = action.value) }
            MergeRequestDetailAction.OnSendComment -> sendComment()
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

    private fun sendComment() {
        val body = _state.value.commentText.trim()
        if (body.isEmpty() || _state.value.actionInProgress) return
        _state.update { it.copy(isCommenting = true) }
        viewModelScope.launch {
            repository.comment(projectId, iid, body)
                .onSuccess {
                    _state.update { it.copy(isCommenting = false, commentText = "") }
                    _events.send(MergeRequestDetailEvent.ShowMessage("Comment posted"))
                    reload()
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
            _state.update { it.copy(isLoading = false) }
        }
    }
}
