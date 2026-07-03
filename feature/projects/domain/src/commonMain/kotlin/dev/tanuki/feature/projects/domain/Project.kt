package dev.tanuki.feature.projects.domain

data class Project(
    val id: Long,
    val name: String,
    val pathWithNamespace: String,
    val description: String?,
    val starCount: Int,
    val lastActivity: String?,
    val webUrl: String,
)
