package dev.tanuki.feature.mergerequests.data.dto

import dev.tanuki.feature.mergerequests.domain.MrCommit
import dev.tanuki.feature.mergerequests.domain.MrPipeline
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

fun MrPipelineDto.toMrPipeline(): MrPipeline = MrPipeline(
    id = id,
    status = status,
    ref = ref,
    webUrl = webUrl,
    createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Instant.DISTANT_PAST,
)
