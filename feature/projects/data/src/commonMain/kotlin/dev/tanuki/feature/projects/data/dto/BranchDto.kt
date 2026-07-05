package dev.tanuki.feature.projects.data.dto

import dev.tanuki.feature.projects.domain.Branch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class BranchDto(
    val name: String,
    val default: Boolean = false,
    val protected: Boolean = false,
    val merged: Boolean = false,
    @SerialName("web_url") val webUrl: String = "",
    val commit: BranchCommitDto? = null,
)

@Serializable
data class BranchCommitDto(
    val title: String? = null,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("committed_date") val committedDate: String? = null,
)

/** Minimal projection of an open MR, used to tag branches with their MR. */
@Serializable
data class BranchMrRefDto(
    val iid: Long,
    @SerialName("source_branch") val sourceBranch: String,
)

fun BranchDto.toBranch(openMergeRequestIid: Long?): Branch = Branch(
    name = name,
    isDefault = default,
    isProtected = protected,
    isMerged = merged,
    lastCommitTitle = commit?.title,
    lastCommitAuthor = commit?.authorName,
    lastActivity = commit?.committedDate
        ?.let { runCatching { Instant.parse(it) }.getOrNull() }
        ?: Instant.DISTANT_PAST,
    webUrl = webUrl,
    openMergeRequestIid = openMergeRequestIid,
)
