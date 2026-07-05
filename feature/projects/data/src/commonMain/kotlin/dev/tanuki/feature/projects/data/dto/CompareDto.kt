package dev.tanuki.feature.projects.data.dto

import dev.tanuki.core.data.diff.DiffDto
import kotlinx.serialization.Serializable

@Serializable
data class CompareDto(
    val diffs: List<DiffDto> = emptyList(),
)
