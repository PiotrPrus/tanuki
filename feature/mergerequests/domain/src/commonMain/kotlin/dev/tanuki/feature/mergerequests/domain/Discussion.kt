package dev.tanuki.feature.mergerequests.domain

import kotlin.time.Instant

/** A resolvable thread of notes, optionally anchored to a diff line. */
data class Discussion(
    val id: String,
    val resolvable: Boolean,
    val resolved: Boolean,
    val notes: List<DiscussionNote>,
    /** Diff anchor (null for overall MR comments). */
    val filePath: String?,
    val newLine: Int?,
    val oldLine: Int?,
) {
    val isOnDiff: Boolean get() = filePath != null && (newLine != null || oldLine != null)
}

data class DiscussionNote(
    val id: Long,
    val authorName: String,
    val authorAvatarUrl: String?,
    val body: String,
    val createdAt: Instant,
    val system: Boolean,
)

/** Diff refs required to anchor a new comment to a line. */
data class DiffRefs(
    val baseSha: String,
    val startSha: String,
    val headSha: String,
)

/** Target for a new line comment. [newLine]/[oldLine] identify the anchored diff line. */
data class NewDiffComment(
    val refs: DiffRefs,
    val newPath: String,
    val oldPath: String,
    val newLine: Int?,
    val oldLine: Int?,
)
