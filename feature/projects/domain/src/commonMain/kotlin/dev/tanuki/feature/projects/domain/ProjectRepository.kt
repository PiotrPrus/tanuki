package dev.tanuki.feature.projects.domain

import dev.tanuki.core.domain.diff.FileDiff
import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.Result

interface ProjectRepository {
    /** Projects for the given [filter], most recently active first. */
    suspend fun getProjects(filter: ProjectFilter): Result<List<Project>, DataError.Remote>

    /** Full detail for a single project (the dashboard). */
    suspend fun getProject(projectId: Long): Result<ProjectDetail, DataError.Remote>

    /** Resolve a URL path (e.g. `teamtilt/mobile/tiltandroid`) to its numeric project id. */
    suspend fun resolveProjectId(path: String): Result<Long, DataError.Remote>

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

    /** A single release by its tag name (for its full description). */
    suspend fun getRelease(projectId: Long, tagName: String): Result<Release, DataError.Remote>

    /** Diff of everything between refs [from] and [to] (branches/tags/shas). */
    suspend fun compareRefs(projectId: Long, from: String, to: String): Result<List<FileDiff>, DataError.Remote>

    /** Recent CI pipelines, newest first. */
    suspend fun getPipelines(projectId: Long): Result<List<Pipeline>, DataError.Remote>

    /** Jobs of a single pipeline (for the stage breakdown). */
    suspend fun getPipelineJobs(projectId: Long, pipelineId: Long): Result<List<PipelineJob>, DataError.Remote>

    /** Directory listing at [path] (empty = repo root) on [ref] (empty = default branch). */
    suspend fun getTree(projectId: Long, ref: String, path: String): Result<List<RepoEntry>, DataError.Remote>

    /** Raw text of a file at [filePath] on [ref]. */
    suspend fun getFileContent(projectId: Long, ref: String, filePath: String): Result<String, DataError.Remote>
}
