package dev.tanuki.feature.mergerequests.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MergeRequestDto(
    val id: Long,
    val iid: Long,
    @SerialName("project_id") val projectId: Long,
    val title: String,
    val description: String? = null,
    val state: String? = null,
    @SerialName("web_url") val webUrl: String,
    @SerialName("source_branch") val sourceBranch: String,
    @SerialName("target_branch") val targetBranch: String,
    val draft: Boolean = false,
    @SerialName("has_conflicts") val hasConflicts: Boolean = false,
    @SerialName("user_notes_count") val userNotesCount: Int = 0,
    @SerialName("detailed_merge_status") val detailedMergeStatus: String? = null,
    @SerialName("blocking_discussions_resolved") val blockingDiscussionsResolved: Boolean = true,
    @SerialName("updated_at") val updatedAt: String,
    val author: AuthorDto,
    val references: ReferencesDto? = null,
    @SerialName("diff_refs") val diffRefs: DiffRefsDto? = null,
    val reviewers: List<AuthorDto> = emptyList(),
)

@Serializable
data class DiffRefsDto(
    @SerialName("base_sha") val baseSha: String? = null,
    @SerialName("start_sha") val startSha: String? = null,
    @SerialName("head_sha") val headSha: String? = null,
)

@Serializable
data class AuthorDto(
    val name: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class ReferencesDto(
    val full: String? = null,
)
