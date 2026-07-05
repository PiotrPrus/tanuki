package dev.tanuki.core.data.diff

import dev.tanuki.core.domain.diff.DiffLine
import dev.tanuki.core.domain.diff.DiffLineType
import dev.tanuki.core.domain.diff.FileDiff
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiffDto(
    @SerialName("old_path") val oldPath: String,
    @SerialName("new_path") val newPath: String,
    @SerialName("new_file") val newFile: Boolean = false,
    @SerialName("renamed_file") val renamedFile: Boolean = false,
    @SerialName("deleted_file") val deletedFile: Boolean = false,
    val diff: String = "",
)

private val HUNK_REGEX = Regex("""@@ -(\d+)(?:,\d+)? \+(\d+)(?:,\d+)? @@.*""")

fun DiffDto.toFileDiff(): FileDiff {
    val parsed = parseUnifiedDiff(diff)
    return FileDiff(
        oldPath = oldPath,
        newPath = newPath,
        isNew = newFile,
        isDeleted = deletedFile,
        isRenamed = renamedFile,
        additions = parsed.count { it.type == DiffLineType.ADDITION },
        deletions = parsed.count { it.type == DiffLineType.DELETION },
        lines = parsed,
    )
}

/** Parses a unified diff string into typed lines, tracking old/new line numbers. */
private fun parseUnifiedDiff(diff: String): List<DiffLine> {
    if (diff.isEmpty()) return emptyList()
    val out = mutableListOf<DiffLine>()
    var oldLine = 0
    var newLine = 0
    for (raw in diff.split("\n")) {
        when {
            raw.startsWith("@@") -> {
                HUNK_REGEX.find(raw)?.let { m ->
                    oldLine = m.groupValues[1].toIntOrNull() ?: oldLine
                    newLine = m.groupValues[2].toIntOrNull() ?: newLine
                }
                out += DiffLine(DiffLineType.HUNK, raw, null, null)
            }
            raw.startsWith("+") -> {
                out += DiffLine(DiffLineType.ADDITION, raw.substring(1), null, newLine)
                newLine++
            }
            raw.startsWith("-") -> {
                out += DiffLine(DiffLineType.DELETION, raw.substring(1), oldLine, null)
                oldLine++
            }
            raw.startsWith("\\") -> Unit // "\ No newline at end of file"
            else -> {
                val content = if (raw.startsWith(" ")) raw.substring(1) else raw
                out += DiffLine(DiffLineType.CONTEXT, content, oldLine, newLine)
                oldLine++
                newLine++
            }
        }
    }
    return out
}
