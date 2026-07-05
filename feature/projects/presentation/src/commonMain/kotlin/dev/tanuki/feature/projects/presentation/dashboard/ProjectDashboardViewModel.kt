package dev.tanuki.feature.projects.presentation.dashboard

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

class ProjectDashboardViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectDashboardState())
    val state = _state.asStateFlow()

    private val _events = Channel<ProjectDashboardEvent>()
    val events = _events.receiveAsFlow()

    private var projectId: Long = 0

    fun load(projectId: Long) {
        this.projectId = projectId
        reload()
    }

    fun onAction(action: ProjectDashboardAction) {
        when (action) {
            ProjectDashboardAction.OnRetry -> reload()
            ProjectDashboardAction.OnOpenInBrowser -> _state.value.detail?.let { detail ->
                viewModelScope.launch { _events.send(ProjectDashboardEvent.OpenInBrowser(detail.webUrl)) }
            }
        }
    }

    private fun reload() {
        _state.update { it.copy(isLoading = true, error = null, stats = null) }
        viewModelScope.launch {
            repository.getProject(projectId)
                .onSuccess { detail ->
                    _state.update { it.copy(isLoading = false, detail = detail) }
                    // Tile counts/status stream in after the detail is on screen.
                    val stats = repository.getProjectStats(projectId, detail.defaultBranch)
                    _state.update { it.copy(stats = stats) }
                }
                .onFailure {
                    _state.update {
                        it.copy(isLoading = false, error = UiText.Dynamic("Couldn't load this project."))
                    }
                }
        }
    }
}
