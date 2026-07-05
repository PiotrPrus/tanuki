package dev.tanuki.feature.projects.domain

/** Best-effort counts/status for the dashboard tiles; any field is null if unavailable. */
data class ProjectStats(
    val openMergeRequests: Int? = null,
    val branches: Int? = null,
    val tags: Int? = null,
    val latestTag: String? = null,
    val releases: Int? = null,
    val contributors: Int? = null,
    val latestPipeline: PipelineStatus? = null,
    /** Daily commit counts over the recent window, oldest first (empty until loaded). */
    val commitActivity: List<Int> = emptyList(),
)

enum class PipelineStatus { SUCCESS, FAILED, RUNNING, OTHER }
