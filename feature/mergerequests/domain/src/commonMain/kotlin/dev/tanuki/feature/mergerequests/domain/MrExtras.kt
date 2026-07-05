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
