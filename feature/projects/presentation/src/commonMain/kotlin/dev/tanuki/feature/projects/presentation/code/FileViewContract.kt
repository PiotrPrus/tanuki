package dev.tanuki.feature.projects.presentation.code

import dev.tanuki.core.presentation.UiText

data class FileViewState(
    val fileName: String = "",
    val isLoading: Boolean = true,
    val content: String? = null,
    val error: UiText? = null,
)

sealed interface FileViewAction {
    data object OnRetry : FileViewAction
}
