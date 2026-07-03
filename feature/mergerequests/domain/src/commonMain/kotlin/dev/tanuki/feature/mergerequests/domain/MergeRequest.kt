package dev.tanuki.feature.mergerequests.domain

import kotlin.time.Instant

data class MergeRequest(
    val id: Long,
    val iid: Long,
    val projectId: Long,
    val title: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val sourceBranch: String,
    val targetBranch: String,
    val reference: String,
    val webUrl: String,
    val status: MergeStatus,
    val isDraft: Boolean,
    val hasConflicts: Boolean,
    val hasUnresolvedDiscussions: Boolean,
    val commentCount: Int,
    val updatedAt: Instant,
)

/** Normalised from GitLab's `detailed_merge_status`. */
enum class MergeStatus {
    MERGEABLE,
    NEEDS_REBASE,
    DISCUSSIONS_UNRESOLVED,
    CI_RUNNING,
    DRAFT,
    CONFLICTS,
    BLOCKED,
    UNKNOWN,
}
