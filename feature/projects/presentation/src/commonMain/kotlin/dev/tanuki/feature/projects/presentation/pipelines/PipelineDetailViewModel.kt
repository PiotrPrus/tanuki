package dev.tanuki.feature.projects.presentation.pipelines

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

class PipelineDetailViewModel(
    private val repository: ProjectRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PipelineDetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<PipelineDetailEvent>()
    val events = _events.receiveAsFlow()

    private var projectId: Long = 0
    private var pipelineId: Long = 0

    fun load(projectId: Long, pipelineId: Long, title: String) {
        this.projectId = projectId
        this.pipelineId = pipelineId
        _state.update { it.copy(title = title) }
        reload()
    }

    fun onAction(action: PipelineDetailAction) {
        when (action) {
            PipelineDetailAction.OnRetry -> reload()
            is PipelineDetailAction.OnOpenJob -> action.job.webUrl?.let { url ->
                viewModelScope.launch { _events.send(PipelineDetailEvent.OpenInBrowser(url)) }
            }
        }
    }

    private fun reload() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getPipelineJobs(projectId, pipelineId)
                .onSuccess { list -> _state.update { it.copy(isLoading = false, jobs = list) } }
                .onFailure {
                    _state.update { it.copy(isLoading = false, error = UiText.Dynamic("Couldn't load jobs.")) }
                }
        }
    }
}
