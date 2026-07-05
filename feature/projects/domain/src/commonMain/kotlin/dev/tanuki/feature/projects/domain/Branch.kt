package dev.tanuki.feature.projects.domain

import kotlin.time.Instant

data class Branch(
    val name: String,
    val isDefault: Boolean,
    val isProtected: Boolean,
    val isMerged: Boolean,
    val lastCommitTitle: String?,
    val lastCommitAuthor: String?,
    val lastCommitAuthorEmail: String?,
    val lastActivity: Instant,
    val webUrl: String,
    /** iid of the open merge request whose source is this branch, if any. */
    val openMergeRequestIid: Long?,
    /** Resolved avatar for the last committer, when known. */
    val authorAvatarUrl: String?,
    /** True when the last commit was authored by the signed-in user. */
    val isMine: Boolean,
)
