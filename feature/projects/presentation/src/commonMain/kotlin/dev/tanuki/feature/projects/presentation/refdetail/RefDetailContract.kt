package dev.tanuki.feature.projects.presentation.refdetail

import dev.tanuki.core.domain.diff.FileDiff
import dev.tanuki.core.presentation.UiText

data class RefDetailState(
    val title: String = "",
    val isLoading: Boolean = true,
    val description: String? = null,
    val diffs: List<FileDiff> = emptyList(),
    val hasComparison: Boolean = true,
    val error: UiText? = null,
) {
    val additions: Int get() = diffs.sumOf { it.additions }
    val deletions: Int get() = diffs.sumOf { it.deletions }
}

sealed interface RefDetailAction {
    data object OnRetry : RefDetailAction
}
