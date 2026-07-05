package dev.tanuki.feature.projects.data

import dev.tanuki.core.data.network.safeCall
import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.Result
import dev.tanuki.core.domain.util.map
import dev.tanuki.feature.projects.data.dto.ProjectDetailDto
import dev.tanuki.feature.projects.data.dto.ProjectDto
import dev.tanuki.feature.projects.data.dto.toProject
import dev.tanuki.feature.projects.data.dto.toProjectDetail
import dev.tanuki.feature.projects.domain.Project
import dev.tanuki.feature.projects.domain.ProjectDetail
import dev.tanuki.feature.projects.domain.ProjectFilter
import dev.tanuki.feature.projects.domain.ProjectRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ProjectRepositoryImpl(
    private val httpClient: HttpClient,
) : ProjectRepository {

    override suspend fun getProjects(filter: ProjectFilter): Result<List<Project>, DataError.Remote> =
        safeCall<List<ProjectDto>> {
            httpClient.get("projects") {
                when (filter) {
                    ProjectFilter.ALL, ProjectFilter.SHARED -> parameter("membership", true)
                    ProjectFilter.STARRED -> parameter("starred", true)
                    ProjectFilter.PERSONAL -> parameter("owned", true)
                }
                parameter("order_by", "last_activity_at")
                parameter("per_page", 30)
            }
        }.map { dtos ->
            dtos.map { it.toProject() }
                // "Shared" = projects you're a member of that live under a group (not your own).
                .filter { filter != ProjectFilter.SHARED || it.namespaceKind == "group" }
        }

    override suspend fun getProject(projectId: Long): Result<ProjectDetail, DataError.Remote> =
        safeCall<ProjectDetailDto> {
            httpClient.get("projects/$projectId")
        }.map { it.toProjectDetail() }
}
