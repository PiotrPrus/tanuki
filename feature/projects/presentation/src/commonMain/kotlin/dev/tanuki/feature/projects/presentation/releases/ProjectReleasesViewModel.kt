package dev.tanuki.feature.projects.presentation.releases

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

class ProjectReleasesViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectReleasesState())
    val state = _state.asStateFlow()

    private val _events = Channel<ProjectReleasesEvent>()
    val events = _events.receiveAsFlow()

    private var projectId: Long = 0

    fun load(projectId: Long) {
        this.projectId = projectId
        reload()
    }

    fun onAction(action: ProjectReleasesAction) {
        when (action) {
            ProjectReleasesAction.OnRetry -> reload()
            is ProjectReleasesAction.OnOpen -> action.release.webUrl?.let { url ->
                viewModelScope.launch { _events.send(ProjectReleasesEvent.OpenInBrowser(url)) }
            }
        }
    }

    private fun reload() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getReleases(projectId)
                .onSuccess { list -> _state.update { it.copy(isLoading = false, releases = list) } }
                .onFailure {
                    _state.update { it.copy(isLoading = false, error = UiText.Dynamic("Couldn't load releases.")) }
                }
        }
    }
}
