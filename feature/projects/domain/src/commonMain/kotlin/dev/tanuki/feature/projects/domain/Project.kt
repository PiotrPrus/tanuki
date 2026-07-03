package dev.tanuki.feature.projects.domain

data class Project(
    val id: Long,
    val name: String,
    val pathWithNamespace: String,
    val description: String?,
    val starCount: Int,
    val lastActivity: String?,
    val webUrl: String,
    val visibility: Visibility,
    /** "user" or "group" — used to tell personal from shared projects. */
    val namespaceKind: String,
)

enum class Visibility { PUBLIC, INTERNAL, PRIVATE, UNKNOWN }
