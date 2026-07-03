package dev.tanuki.feature.projects.data

import dev.tanuki.core.data.network.safeCall
import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.Result
import dev.tanuki.core.domain.util.map
import dev.tanuki.feature.projects.data.dto.ProjectDto
import dev.tanuki.feature.projects.data.dto.toProject
import dev.tanuki.feature.projects.domain.Project
import dev.tanuki.feature.projects.domain.ProjectRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ProjectRepositoryImpl(
    private val httpClient: HttpClient,
) : ProjectRepository {

    override suspend fun getMyProjects(): Result<List<Project>, DataError.Remote> =
        safeCall<List<ProjectDto>> {
            httpClient.get("projects") {
                parameter("membership", true)
                parameter("simple", true)
                parameter("order_by", "last_activity_at")
                parameter("per_page", 30)
            }
        }.map { dtos -> dtos.map { it.toProject() } }
}
