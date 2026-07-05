package dev.tanuki.feature.projects.domain

data class ProjectDetail(
    val id: Long,
    val name: String,
    val pathWithNamespace: String,
    val description: String?,
    val visibility: Visibility,
    val starCount: Int,
    val forksCount: Int,
    val openIssuesCount: Int,
    val defaultBranch: String?,
    val webUrl: String,
)
