package dev.tanuki.feature.projects.domain

/** One entry in a repository directory listing. */
data class RepoEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
)
