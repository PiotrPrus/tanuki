package dev.tanuki.feature.mergerequests.presentation.projectlist

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

class ProjectMergeRequestsViewModel(
    private val repository: MergeRequestRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectMergeRequestsState())
    val state = _state.asStateFlow()

    private val _events = Channel<ProjectMergeRequestsEvent>()
    val events = _events.receiveAsFlow()

    private var projectId: Long = 0

    fun load(projectId: Long) {
        this.projectId = projectId
        reload()
    }

    fun onAction(action: ProjectMergeRequestsAction) {
        when (action) {
            ProjectMergeRequestsAction.OnRetry -> reload()
            is ProjectMergeRequestsAction.OnSelectFilter -> {
                if (action.filter == _state.value.filter) return
                _state.update { it.copy(filter = action.filter) }
                reload()
            }
            is ProjectMergeRequestsAction.OnOpen -> viewModelScope.launch {
                _events.send(
                    ProjectMergeRequestsEvent.OpenDetail(
                        projectId = action.mergeRequest.projectId,
                        iid = action.mergeRequest.iid,
                    ),
                )
            }
        }
    }

    private fun reload() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getProjectMergeRequests(projectId, _state.value.filter)
                .onSuccess { list ->
                    _state.update { it.copy(isLoading = false, mergeRequests = list) }
                }
                .onFailure {
                    _state.update {
                        it.copy(isLoading = false, error = UiText.Dynamic("Couldn't load merge requests."))
                    }
                }
        }
    }
}
