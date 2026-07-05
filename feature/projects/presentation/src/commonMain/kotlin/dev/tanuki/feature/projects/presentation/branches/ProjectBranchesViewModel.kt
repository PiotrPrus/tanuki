package dev.tanuki.feature.projects.presentation.branches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tanuki.core.domain.util.onFailure
import dev.tanuki.core.domain.util.onSuccess
import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.ProjectRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectBranchesViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectBranchesState())
    val state = _state.asStateFlow()

    private val _events = Channel<ProjectBranchesEvent>()
    val events = _events.receiveAsFlow()

    private var projectId: Long = 0

    fun load(projectId: Long) {
        this.projectId = projectId
        reload()
    }

    fun onAction(action: ProjectBranchesAction) {
        when (action) {
            ProjectBranchesAction.OnRetry -> reload()
            is ProjectBranchesAction.OnOpenBranch -> viewModelScope.launch {
                _events.send(ProjectBranchesEvent.OpenInBrowser(action.branch.webUrl))
            }
            is ProjectBranchesAction.OnOpenMergeRequest -> viewModelScope.launch {
                _events.send(ProjectBranchesEvent.OpenMergeRequest(projectId, action.iid))
            }
        }
    }

    private fun reload() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getBranches(projectId)
                .onSuccess { list -> _state.update { it.copy(isLoading = false, branches = list) } }
                .onFailure {
                    _state.update {
                        it.copy(isLoading = false, error = UiText.Dynamic("Couldn't load branches."))
                    }
                }
        }
    }
}
