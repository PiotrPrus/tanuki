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

class FileViewViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FileViewState())
    val state = _state.asStateFlow()

    private var projectId: Long = 0
    private var ref: String = ""
    private var filePath: String = ""

    fun load(projectId: Long, ref: String, filePath: String, fileName: String) {
        this.projectId = projectId
        this.ref = ref
        this.filePath = filePath
        _state.update { it.copy(fileName = fileName) }
        reload()
    }

    fun onAction(action: FileViewAction) {
        when (action) {
            FileViewAction.OnRetry -> reload()
        }
    }

    private fun reload() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getFileContent(projectId, ref, filePath)
                .onSuccess { text -> _state.update { it.copy(isLoading = false, content = text) } }
                .onFailure {
                    _state.update { it.copy(isLoading = false, error = UiText.Dynamic("Couldn't load this file.")) }
                }
        }
    }
}
