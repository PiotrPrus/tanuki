package dev.tanuki.feature.mergerequests.data.mapper

import dev.tanuki.feature.mergerequests.data.dto.MergeRequestDto
import dev.tanuki.feature.mergerequests.domain.DiffRefs
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeRequestState
import dev.tanuki.feature.mergerequests.domain.MergeStatus
import dev.tanuki.feature.mergerequests.domain.MrUser
import kotlin.time.Instant

fun MergeRequestDto.toMergeRequest(): MergeRequest = MergeRequest(
    id = id,
    iid = iid,
    projectId = projectId,
    title = title,
    authorName = author.name,
    authorAvatarUrl = author.avatarUrl,
    sourceBranch = sourceBranch,
    targetBranch = targetBranch,
    reference = references?.full ?: "!$iid",
    webUrl = webUrl,
    description = description,
    state = when (state) {
        "opened" -> MergeRequestState.OPEN
        "merged" -> MergeRequestState.MERGED
        "closed" -> MergeRequestState.CLOSED
        else -> MergeRequestState.UNKNOWN
    },
    status = detailedMergeStatus.toMergeStatus(isDraft = draft, hasConflicts = hasConflicts),
    isDraft = draft,
    hasConflicts = hasConflicts,
    hasUnresolvedDiscussions = !blockingDiscussionsResolved,
    commentCount = userNotesCount,
    updatedAt = runCatching { Instant.parse(updatedAt) }.getOrDefault(Instant.DISTANT_PAST),
    diffRefs = diffRefs?.let { r ->
        if (r.baseSha != null && r.startSha != null && r.headSha != null) {
            DiffRefs(baseSha = r.baseSha, startSha = r.startSha, headSha = r.headSha)
        } else {
            null
        }
    },
    reviewers = reviewers.map { MrUser(name = it.name, avatarUrl = it.avatarUrl) },
)

private fun String?.toMergeStatus(isDraft: Boolean, hasConflicts: Boolean): MergeStatus = when {
    isDraft -> MergeStatus.DRAFT
    hasConflicts -> MergeStatus.CONFLICTS
    else -> when (this) {
        "mergeable" -> MergeStatus.MERGEABLE
        "need_rebase" -> MergeStatus.NEEDS_REBASE
        "discussions_not_resolved" -> MergeStatus.DISCUSSIONS_UNRESOLVED
        "ci_still_running", "ci_must_pass" -> MergeStatus.CI_RUNNING
        "not_approved", "blocked_status" -> MergeStatus.BLOCKED
        null -> MergeStatus.UNKNOWN
        else -> MergeStatus.UNKNOWN
    }
}
