package dev.tanuki.feature.mergerequests.domain

import kotlin.time.Instant

data class MrCommit(
    val shortId: String,
    val title: String,
    val authorName: String?,
    val createdAt: Instant,
)

data class MrPipeline(
    val id: Long,
    val status: String,
    val ref: String,
    val webUrl: String?,
    val createdAt: Instant,
)

data class MrUser(
    val name: String,
    val avatarUrl: String?,
)

/** MR approval state: who approved and how many approvals remain. */
data class ApprovalInfo(
    val approved: Boolean,
    val approvalsRequired: Int,
    val approvalsLeft: Int,
    val approvedBy: List<MrUser>,
)
