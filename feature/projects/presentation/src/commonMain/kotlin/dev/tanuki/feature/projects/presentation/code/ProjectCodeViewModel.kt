package dev.tanuki.feature.projects.presentation.code

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

class ProjectCodeViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectCodeState())
    val state = _state.asStateFlow()

    private var projectId: Long = 0
    private var ref: String = ""
    private var path: String = ""

    fun load(projectId: Long, ref: String, path: String) {
        this.projectId = projectId
        this.ref = ref
        this.path = path
        reload()
    }

    fun onAction(action: ProjectCodeAction) {
        when (action) {
            ProjectCodeAction.OnRetry -> reload()
        }
    }

    private fun reload() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getTree(projectId, ref, path)
                .onSuccess { list -> _state.update { it.copy(isLoading = false, entries = list) } }
                .onFailure {
                    _state.update { it.copy(isLoading = false, error = UiText.Dynamic("Couldn't load this folder.")) }
                }
        }
    }
}
