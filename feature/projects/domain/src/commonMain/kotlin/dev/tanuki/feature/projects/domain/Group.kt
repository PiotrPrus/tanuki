package dev.tanuki.feature.projects.domain

/** A GitLab group or subgroup — a node in the project navigation hierarchy. */
data class Group(
    val id: Long,
    val name: String,
    /** Full URL path, e.g. "teamtilt/web". Also usable (URL-encoded) as the API :id. */
    val fullPath: String,
    val description: String?,
    val parentId: Long?,
    val avatarUrl: String?,
)
