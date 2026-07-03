package dev.tanuki.core.presentation

import androidx.compose.runtime.Composable

/** Presentation-layer string wrapper so ViewModels can emit UI text without a Context. */
sealed interface UiText {
    data class Dynamic(val value: String) : UiText

    @Composable
    fun asString(): String = when (this) {
        is Dynamic -> value
    }
}
