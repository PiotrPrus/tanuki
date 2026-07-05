package dev.tanuki.feature.projects.presentation.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tanuki.core.domain.util.onFailure
import dev.tanuki.core.domain.util.onSuccess
import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectTagsViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectTagsState())
    val state = _state.asStateFlow()

    private var projectId: Long = 0

    fun load(projectId: Long) {
        this.projectId = projectId
        reload()
    }

    fun onAction(action: ProjectTagsAction) {
        when (action) {
            ProjectTagsAction.OnRetry -> reload()
        }
    }

    private fun reload() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getTags(projectId)
                .onSuccess { list -> _state.update { it.copy(isLoading = false, tags = list) } }
                .onFailure {
                    _state.update { it.copy(isLoading = false, error = UiText.Dynamic("Couldn't load tags.")) }
                }
        }
    }
}
