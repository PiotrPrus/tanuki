package dev.tanuki.feature.projects.domain

import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.Result

interface ProjectRepository {
    /** Projects for the given [filter], most recently active first. */
    suspend fun getProjects(filter: ProjectFilter): Result<List<Project>, DataError.Remote>

    /** Full detail for a single project (the dashboard). */
    suspend fun getProject(projectId: Long): Result<ProjectDetail, DataError.Remote>

    /** Best-effort dashboard counts/status (parallel calls; nulls where unavailable). */
    suspend fun getProjectStats(projectId: Long, defaultBranch: String?): ProjectStats

    /** Repository branches, annotated with any open merge request sourced from each. */
    suspend fun getBranches(projectId: Long): Result<List<Branch>, DataError.Remote>

    /** Create a branch [name] from [ref] (an existing branch/tag/sha). */
    suspend fun createBranch(projectId: Long, name: String, ref: String): Result<Branch, DataError.Remote>

    /** Repository tags, newest first. */
    suspend fun getTags(projectId: Long): Result<List<Tag>, DataError.Remote>

    /** Project releases, newest first. */
    suspend fun getReleases(projectId: Long): Result<List<Release>, DataError.Remote>
}
