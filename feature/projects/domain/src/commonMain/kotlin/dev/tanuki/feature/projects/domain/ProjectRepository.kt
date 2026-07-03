package dev.tanuki.feature.projects.domain

import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.Result

interface ProjectRepository {
    /** Projects the current user is a member of, most recently active first. */
    suspend fun getMyProjects(): Result<List<Project>, DataError.Remote>
}
