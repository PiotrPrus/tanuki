package dev.tanuki.feature.mergerequests.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tanuki.core.domain.util.Result
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

class MergeRequestsViewModel(
    private val repository: MergeRequestRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MergeRequestsState())
    val state = _state.asStateFlow()

    private val _events = Channel<MergeRequestsEvent>()
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: MergeRequestsAction) {
        when (action) {
            MergeRequestsAction.OnRefresh -> load()
            is MergeRequestsAction.OnSelectScope -> {
                if (action.scope == _state.value.scope) return
                _state.update { it.copy(scope = action.scope) }
                load()
            }
            is MergeRequestsAction.OnOpen -> viewModelScope.launch {
                _events.send(
                    MergeRequestsEvent.OpenDetail(
                        projectId = action.mergeRequest.projectId,
                        iid = action.mergeRequest.iid,
                    ),
                )
            }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = when (_state.value.scope) {
                ReviewScope.ASSIGNED_TO_ME -> repository.getAssignedToMe()
                ReviewScope.REVIEW_REQUESTED -> repository.getReviewRequested()
            }
            result
                .onSuccess { list ->
                    _state.update { it.copy(isLoading = false, mergeRequests = list) }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.Dynamic("Couldn't load merge requests."),
                        )
                    }
                }
        }
    }
}
