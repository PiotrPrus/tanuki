package dev.tanuki.feature.projects.data.dto

import dev.tanuki.feature.projects.domain.Release
import dev.tanuki.feature.projects.domain.Tag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class TagDto(
    val name: String,
    val message: String? = null,
    val protected: Boolean = false,
    val commit: BranchCommitDto? = null,
    val release: TagReleaseDto? = null,
)

@Serializable
data class TagReleaseDto(@SerialName("tag_name") val tagName: String? = null)

@Serializable
data class PipelineDto(val status: String? = null)

@Serializable
data class ReleaseDto(
    @SerialName("tag_name") val tagName: String = "",
    val name: String? = null,
    val description: String? = null,
    @SerialName("released_at") val releasedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val author: MrAuthorDto? = null,
    val assets: ReleaseAssetsDto? = null,
    @SerialName("_links") val links: ReleaseLinksDto? = null,
)

@Serializable
data class ReleaseAssetsDto(val count: Int = 0)

@Serializable
data class ReleaseLinksDto(val self: String? = null)

@Serializable
data class CommitDto(@SerialName("created_at") val createdAt: String? = null)

fun TagDto.toTag(): Tag = Tag(
    name = name,
    message = message?.takeIf { it.isNotBlank() },
    isProtected = protected,
    commitTitle = commit?.title,
    commitAuthor = commit?.authorName,
    lastActivity = commit?.committedDate
        ?.let { runCatching { Instant.parse(it) }.getOrNull() }
        ?: Instant.DISTANT_PAST,
    hasRelease = release?.tagName != null,
)

fun ReleaseDto.toRelease(): Release = Release(
    tagName = tagName,
    name = name?.takeIf { it.isNotBlank() } ?: tagName,
    description = description?.takeIf { it.isNotBlank() },
    releasedAt = (releasedAt ?: createdAt)
        ?.let { runCatching { Instant.parse(it) }.getOrNull() }
        ?: Instant.DISTANT_PAST,
    authorName = author?.name,
    authorAvatarUrl = author?.avatarUrl,
    assetCount = assets?.count ?: 0,
    webUrl = links?.self,
)
