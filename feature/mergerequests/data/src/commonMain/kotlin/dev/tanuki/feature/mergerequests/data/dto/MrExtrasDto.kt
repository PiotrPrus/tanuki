package dev.tanuki.feature.mergerequests.data.dto

import dev.tanuki.feature.mergerequests.domain.ApprovalInfo
import dev.tanuki.feature.mergerequests.domain.MrCommit
import dev.tanuki.feature.mergerequests.domain.MrPipeline
import dev.tanuki.feature.mergerequests.domain.MrUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class MrCommitDto(
    @SerialName("short_id") val shortId: String = "",
    val title: String = "",
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class MrPipelineDto(
    val id: Long,
    val status: String = "",
    val ref: String = "",
    @SerialName("web_url") val webUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

fun MrCommitDto.toMrCommit(): MrCommit = MrCommit(
    shortId = shortId,
    title = title,
    authorName = authorName,
    createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Instant.DISTANT_PAST,
)

@Serializable
data class ApprovalsDto(
    val approved: Boolean = false,
    @SerialName("approvals_required") val approvalsRequired: Int = 0,
    @SerialName("approvals_left") val approvalsLeft: Int = 0,
    @SerialName("approved_by") val approvedBy: List<ApprovedByDto> = emptyList(),
)

@Serializable
data class ApprovedByDto(val user: AuthorDto? = null)

fun ApprovalsDto.toApprovalInfo(): ApprovalInfo = ApprovalInfo(
    approved = approved,
    approvalsRequired = approvalsRequired,
    approvalsLeft = approvalsLeft,
    approvedBy = approvedBy.mapNotNull { it.user?.let { u -> MrUser(u.name, u.avatarUrl) } },
)

fun MrPipelineDto.toMrPipeline(): MrPipeline = MrPipeline(
    id = id,
    status = status,
    ref = ref,
    webUrl = webUrl,
    createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Instant.DISTANT_PAST,
)
