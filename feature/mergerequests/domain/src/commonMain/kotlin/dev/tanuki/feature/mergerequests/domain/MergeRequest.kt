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
    val description: String?,
    val state: MergeRequestState,
    val status: MergeStatus,
    val isDraft: Boolean,
    val hasConflicts: Boolean,
    val hasUnresolvedDiscussions: Boolean,
    val commentCount: Int,
    val updatedAt: Instant,
    val diffRefs: DiffRefs?,
    val reviewers: List<MrUser>,
    /** Status of the MR's head pipeline (e.g. "success", "running", "failed"), if any. */
    val headPipelineStatus: String?,
    /** True while a rebase requested through the API is still running. */
    val rebaseInProgress: Boolean,
    /** Error from the last merge/rebase attempt, if any. */
    val mergeError: String?,
    /** How many commits the source branch is behind the target (null if unknown). */
    val commitsBehind: Int?,
)

/** The MR's lifecycle state, from GitLab's `state` field. */
enum class MergeRequestState { OPEN, MERGED, CLOSED, UNKNOWN }

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
