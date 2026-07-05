package dev.tanuki.feature.projects.domain

import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.Result

interface ProjectRepository {
    /** Projects for the given [filter], most recently active first. */
    suspend fun getProjects(filter: ProjectFilter): Result<List<Project>, DataError.Remote>

    /** Full detail for a single project (the dashboard). */
    suspend fun getProject(projectId: Long): Result<ProjectDetail, DataError.Remote>
}
