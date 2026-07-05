package dev.tanuki.feature.mergerequests.data.dto

import dev.tanuki.feature.mergerequests.domain.Discussion
import dev.tanuki.feature.mergerequests.domain.DiscussionNote
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class DiscussionDto(
    val id: String,
    val notes: List<NoteDto> = emptyList(),
)

@Serializable
data class NoteDto(
    val id: Long,
    val body: String = "",
    val system: Boolean = false,
    val resolvable: Boolean = false,
    val resolved: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    val author: AuthorDto,
    val position: PositionDto? = null,
)

@Serializable
data class PositionDto(
    @SerialName("new_path") val newPath: String? = null,
    @SerialName("old_path") val oldPath: String? = null,
    @SerialName("new_line") val newLine: Int? = null,
    @SerialName("old_line") val oldLine: Int? = null,
)

fun DiscussionDto.toDiscussion(): Discussion {
    val position = notes.firstNotNullOfOrNull { it.position }
    val resolvableNotes = notes.filter { it.resolvable }
    return Discussion(
        id = id,
        resolvable = resolvableNotes.isNotEmpty(),
        resolved = resolvableNotes.isNotEmpty() && resolvableNotes.all { it.resolved },
        notes = notes.map { it.toNote() },
        filePath = position?.newPath ?: position?.oldPath,
        newLine = position?.newLine,
        oldLine = position?.oldLine,
    )
}

private fun NoteDto.toNote(): DiscussionNote = DiscussionNote(
    id = id,
    authorName = author.name,
    authorAvatarUrl = author.avatarUrl,
    body = body,
    createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Instant.DISTANT_PAST,
    system = system,
)
