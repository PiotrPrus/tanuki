package dev.tanuki.feature.projects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tanuki.core.domain.util.onFailure
import dev.tanuki.core.domain.util.onSuccess
import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.Project
import dev.tanuki.feature.projects.domain.ProjectRepository
import kotlinx.coroutines.async
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

    private var starredIds: Set<Long> = emptySet()

    init {
        load()
    }

    fun onAction(action: ProjectsAction) {
        when (action) {
            ProjectsAction.OnRefresh -> load()
            is ProjectsAction.OnFilterChange -> {
                if (action.filter != _state.value.filter) {
                    _state.update { it.copy(filter = action.filter, query = "") }
                    load()
                }
            }
            is ProjectsAction.OnQueryChange -> _state.update { it.copy(query = action.query) }
            is ProjectsAction.OnOpenProject -> viewModelScope.launch {
                _events.send(ProjectsEvent.OpenDashboard(action.project.id, action.project.name))
            }
            is ProjectsAction.OnToggleStar -> toggleStar(action.project)
        }
    }

    private fun load() {
        val filter = _state.value.filter
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val projectsDeferred = async { repository.getProjects(filter) }
            repository.getStarredProjectIds().onSuccess { starredIds = it }
            projectsDeferred.await()
                .onSuccess { list ->
                    _state.update { it.copy(isLoading = false, projects = list.withStars()) }
                }
                .onFailure {
                    _state.update {
                        it.copy(isLoading = false, projects = emptyList(), error = UiText.Dynamic("Couldn't load projects."))
                    }
                }
        }
    }

    private fun toggleStar(project: Project) {
        val newStarred = !project.starred
        starredIds = if (newStarred) starredIds + project.id else starredIds - project.id
        patch(project.id, newStarred)
        viewModelScope.launch {
            val result = if (newStarred) repository.starProject(project.id) else repository.unstarProject(project.id)
            result.onFailure {
                starredIds = if (newStarred) starredIds - project.id else starredIds + project.id
                patch(project.id, !newStarred)
            }
        }
    }

    private fun patch(id: Long, starred: Boolean) {
        _state.update { s ->
            s.copy(
                projects = s.projects.map {
                    if (it.id == id && it.starred != starred) {
                        it.copy(starred = starred, starCount = (it.starCount + if (starred) 1 else -1).coerceAtLeast(0))
                    } else {
                        it
                    }
                },
            )
        }
    }

    private fun List<Project>.withStars() = map { it.copy(starred = it.id in starredIds) }
}
