package dev.tanuki.feature.projects.domain

import kotlin.time.Instant

data class Release(
    val tagName: String,
    val name: String,
    val description: String?,
    val releasedAt: Instant,
    val authorName: String?,
    val authorAvatarUrl: String?,
    val assetCount: Int,
    val webUrl: String?,
)
