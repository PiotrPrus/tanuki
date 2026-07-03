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
            // Diffs are secondary — a failure here still shows the MR metadata.
            repository.getDiffs(projectId, iid)
                .onSuccess { diffs -> _state.update { it.copy(diffs = diffs) } }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
