package dev.tanuki.feature.projects.presentation.groupbrowser

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

class GroupBrowserViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GroupBrowserState())
    val state = _state.asStateFlow()

    private val _events = Channel<GroupBrowserEvent>()
    val events = _events.receiveAsFlow()

    private var starredIds: Set<Long> = emptySet()
    private var path: String = ""

    fun load(fullPath: String, title: String) {
        path = fullPath
        _state.update { it.copy(fullPath = fullPath, title = title) }
        reload()
    }

    fun onAction(action: GroupBrowserAction) {
        when (action) {
            GroupBrowserAction.OnRetry -> reload()
            is GroupBrowserAction.OnOpenSubgroup -> viewModelScope.launch {
                _events.send(GroupBrowserEvent.OpenGroup(action.group.fullPath, action.group.name))
            }
            is GroupBrowserAction.OnOpenProject -> viewModelScope.launch {
                _events.send(GroupBrowserEvent.OpenDashboard(action.project.id, action.project.name))
            }
            is GroupBrowserAction.OnOpenGroupPath -> viewModelScope.launch {
                _events.send(GroupBrowserEvent.OpenGroup(action.fullPath, action.fullPath.substringAfterLast('/')))
            }
            is GroupBrowserAction.OnToggleStar -> toggleStar(action.project)
        }
    }

    private fun reload() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val subgroupsDeferred = async { repository.getSubgroups(path) }
            val projectsDeferred = async { repository.getGroupProjects(path) }
            repository.getStarredProjectIds().onSuccess { starredIds = it }

            var ok = false
            subgroupsDeferred.await().onSuccess { list -> ok = true; _state.update { it.copy(subgroups = list) } }
            projectsDeferred.await().onSuccess { list ->
                ok = true
                _state.update { it.copy(projects = list.map { p -> p.copy(starred = p.id in starredIds) }) }
            }
            _state.update {
                it.copy(isLoading = false, error = if (!ok) UiText.Dynamic("Couldn't load this group.") else null)
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
}
