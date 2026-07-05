package dev.tanuki.feature.projects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tanuki.core.domain.util.onFailure
import dev.tanuki.core.domain.util.onSuccess
import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.ProjectFilter
import dev.tanuki.feature.projects.domain.ProjectRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectsState())
    val state = _state.asStateFlow()

    private val _events = Channel<ProjectsEvent>()
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: ProjectsAction) {
        when (action) {
            ProjectsAction.OnRefresh -> load()
            is ProjectsAction.OnOpen -> viewModelScope.launch {
                _events.send(
                    ProjectsEvent.OpenDashboard(action.project.id, action.project.name),
                )
            }
            is ProjectsAction.OnFilterChange -> {
                if (action.filter != _state.value.filter) {
                    _state.update { it.copy(filter = action.filter) }
                    load()
                }
            }
            is ProjectsAction.OnQueryChange -> _state.update { it.copy(query = action.query) }
        }
    }

    private fun load() {
        val filter = _state.value.filter
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getProjects(filter)
                .onSuccess { list -> _state.update { it.copy(isLoading = false, projects = list) } }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            projects = emptyList(),
                            error = UiText.Dynamic("Couldn't load projects."),
                        )
                    }
                }
        }
    }
}
