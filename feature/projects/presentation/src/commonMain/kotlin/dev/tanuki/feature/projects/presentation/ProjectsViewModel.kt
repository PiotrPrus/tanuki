package dev.tanuki.feature.projects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tanuki.core.domain.util.onFailure
import dev.tanuki.core.domain.util.onSuccess
import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.Project
import dev.tanuki.feature.projects.domain.ProjectFilter
import dev.tanuki.feature.projects.domain.ProjectRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
    private var searchJob: Job? = null

    init {
        load()
    }

    fun onAction(action: ProjectsAction) {
        when (action) {
            ProjectsAction.OnRefresh -> load()
            is ProjectsAction.OnQueryChange -> onQueryChange(action.query)
            is ProjectsAction.OnOpenProject -> viewModelScope.launch {
                _events.send(ProjectsEvent.OpenDashboard(action.project.id, action.project.name))
            }
            is ProjectsAction.OnOpenGroup -> viewModelScope.launch {
                _events.send(ProjectsEvent.OpenGroup(action.group.fullPath))
            }
            is ProjectsAction.OnToggleStar -> toggleStar(action.project)
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val groupsDeferred = async { repository.getTopLevelGroups() }
            val personalDeferred = async { repository.getProjects(ProjectFilter.PERSONAL) }
            repository.getStarredProjectIds().onSuccess { starredIds = it }

            var loadedAnything = false
            groupsDeferred.await().onSuccess { list ->
                loadedAnything = true
                _state.update { it.copy(groups = list) }
            }
            personalDeferred.await().onSuccess { list ->
                loadedAnything = true
                val personal = list.filter { it.namespaceKind == "user" }.withStars()
                _state.update { it.copy(personalProjects = personal) }
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    error = if (!loadedAnything) UiText.Dynamic("Couldn't load projects.") else null,
                )
            }
        }
    }

    private fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(250) // debounce
            _state.update { it.copy(isSearching = true) }
            repository.searchProjects(query)
                .onSuccess { list -> _state.update { it.copy(searchResults = list.withStars(), isSearching = false) } }
                .onFailure { _state.update { it.copy(isSearching = false) } }
        }
    }

    private fun toggleStar(project: Project) {
        val newStarred = !project.starred
        starredIds = if (newStarred) starredIds + project.id else starredIds - project.id
        updateProject(project.id, newStarred)
        viewModelScope.launch {
            val result = if (newStarred) repository.starProject(project.id) else repository.unstarProject(project.id)
            result.onFailure {
                // Revert on failure.
                starredIds = if (newStarred) starredIds - project.id else starredIds + project.id
                updateProject(project.id, !newStarred)
            }
        }
    }

    private fun updateProject(id: Long, starred: Boolean) {
        fun List<Project>.patch() = map {
            if (it.id == id && it.starred != starred) {
                it.copy(starred = starred, starCount = (it.starCount + if (starred) 1 else -1).coerceAtLeast(0))
            } else {
                it
            }
        }
        _state.update { it.copy(personalProjects = it.personalProjects.patch(), searchResults = it.searchResults.patch()) }
    }

    private fun List<Project>.withStars() = map { it.copy(starred = it.id in starredIds) }
}
