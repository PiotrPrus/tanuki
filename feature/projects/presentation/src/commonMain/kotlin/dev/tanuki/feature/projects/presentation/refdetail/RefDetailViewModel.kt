package dev.tanuki.feature.projects.presentation.refdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tanuki.core.domain.util.onSuccess
import dev.tanuki.core.presentation.UiText
import dev.tanuki.feature.projects.domain.ProjectRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RefDetailViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RefDetailState())
    val state = _state.asStateFlow()

    private var projectId: Long = 0
    private var ref: String = ""
    private var fromRef: String? = null

    fun load(projectId: Long, ref: String, fromRef: String?, title: String) {
        this.projectId = projectId
        this.ref = ref
        this.fromRef = fromRef
        _state.update { it.copy(title = title, hasComparison = fromRef != null) }
        reload()
    }

    fun onAction(action: RefDetailAction) {
        when (action) {
            RefDetailAction.OnRetry -> reload()
        }
    }

    private fun reload() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            coroutineScope {
                val descriptionDeferred = async {
                    var description: String? = null
                    repository.getRelease(projectId, ref).onSuccess { description = it.description }
                    description
                }
                val from = fromRef
                val diffsDeferred = async {
                    if (from == null) {
                        emptyList()
                    } else {
                        var diffs = emptyList<dev.tanuki.core.domain.diff.FileDiff>()
                        repository.compareRefs(projectId, from = from, to = ref).onSuccess { diffs = it }
                        diffs
                    }
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        description = descriptionDeferred.await(),
                        diffs = diffsDeferred.await(),
                    )
                }
            }
        }
    }
}
