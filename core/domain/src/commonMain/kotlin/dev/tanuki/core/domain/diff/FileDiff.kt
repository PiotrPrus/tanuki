package dev.tanuki.core.domain.diff

/** One changed file, with its unified diff parsed into [lines]. */
data class FileDiff(
    val oldPath: String,
    val newPath: String,
    val isNew: Boolean,
    val isDeleted: Boolean,
    val isRenamed: Boolean,
    val additions: Int,
    val deletions: Int,
    val lines: List<DiffLine>,
)

data class DiffLine(
    val type: DiffLineType,
    val content: String,
    val oldLine: Int?,
    val newLine: Int?,
)

enum class DiffLineType { ADDITION, DELETION, CONTEXT, HUNK }
