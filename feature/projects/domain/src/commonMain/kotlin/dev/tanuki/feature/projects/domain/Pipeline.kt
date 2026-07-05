package dev.tanuki.feature.projects.domain

import kotlin.time.Instant

data class Pipeline(
    val id: Long,
    val ref: String,
    val sha: String,
    val status: PipelineStatus,
    /** Raw GitLab status ("success", "canceled", "manual", …) for precise display. */
    val statusLabel: String,
    val source: String?,
    val createdAt: Instant,
    val webUrl: String?,
)

data class PipelineJob(
    val id: Long,
    val name: String,
    val stage: String,
    val status: PipelineStatus,
    val statusLabel: String,
    val allowFailure: Boolean,
    val webUrl: String?,
)
