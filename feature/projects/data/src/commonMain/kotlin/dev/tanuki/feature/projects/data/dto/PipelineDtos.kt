package dev.tanuki.feature.projects.data.dto

import dev.tanuki.feature.projects.domain.Pipeline
import dev.tanuki.feature.projects.domain.PipelineJob
import dev.tanuki.feature.projects.domain.PipelineStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class PipelineListItemDto(
    val id: Long,
    val ref: String = "",
    val sha: String = "",
    val status: String? = null,
    val source: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("web_url") val webUrl: String? = null,
)

@Serializable
data class JobDto(
    val id: Long,
    val name: String = "",
    val stage: String = "",
    val status: String? = null,
    @SerialName("allow_failure") val allowFailure: Boolean = false,
    @SerialName("web_url") val webUrl: String? = null,
)

fun PipelineListItemDto.toPipeline(): Pipeline = Pipeline(
    id = id,
    ref = ref,
    sha = sha.take(8),
    status = status.toCiStatus(),
    statusLabel = status ?: "unknown",
    source = source,
    createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Instant.DISTANT_PAST,
    webUrl = webUrl,
)

fun JobDto.toPipelineJob(): PipelineJob = PipelineJob(
    id = id,
    name = name,
    stage = stage,
    status = status.toCiStatus(),
    statusLabel = status ?: "unknown",
    allowFailure = allowFailure,
    webUrl = webUrl,
)

/** GitLab CI status string → coarse [PipelineStatus] for colouring. */
fun String?.toCiStatus(): PipelineStatus = when (this) {
    "success" -> PipelineStatus.SUCCESS
    "failed" -> PipelineStatus.FAILED
    "running", "pending", "created", "preparing", "waiting_for_resource", "scheduled" -> PipelineStatus.RUNNING
    else -> PipelineStatus.OTHER
}
