package dev.tanuki.feature.projects.domain

import kotlin.time.Instant

data class Tag(
    val name: String,
    val message: String?,
    val isProtected: Boolean,
    val commitTitle: String?,
    val commitAuthor: String?,
    val lastActivity: Instant,
    val hasRelease: Boolean,
)
